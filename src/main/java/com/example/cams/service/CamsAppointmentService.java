package com.example.cams.service;

import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.CamsAudit;
import com.example.cams.entity.CamsSlot;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.AppointmentStatus;
import com.example.cams.entity.enums.BookingChannel;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsAppointmentRepository;
import com.example.cams.repository.CamsAuditRepository;
import com.example.cams.repository.CamsLeaveRepository;
import com.example.cams.repository.CamsSlotRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;
import com.example.cams.util.exception.CamsApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.example.cams.service.CamsCatalogService.ACTIVE_STATUSES;
import static com.example.cams.util.CamsValidation.integer;
import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Booking, status transitions, reschedule/cancel and audit trail — mirrors app.py's appointment endpoints. */
@Service
public class CamsAppointmentService {

    private static final Pattern PHONE = Pattern.compile("[+0-9 ()-]{8,20}");
    private static final Set<String> BOOKING_DETAIL_KEYS = Set.of("booking_for", "name", "phone", "birth_year",
            "gender", "province", "ward", "address", "extra_note", "payment");

    private final CamsAppointmentRepository appointmentRepository;
    private final CamsSlotRepository slotRepository;
    private final CamsUserRepository userRepository;
    private final CamsLeaveRepository leaveRepository;
    private final CamsAuditRepository auditRepository;
    private final CamsCatalogService catalogService;
    private final CamsClock clock;

    // A plain, locally-owned mapper: this is only used to serialize our own JSON text columns
    // (booking_details, audit before/after), not Spring MVC's HTTP layer, so it is independent
    // of whichever Jackson generation (2.x classic vs 3.x) Boot wires up for the web layer.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CamsAppointmentService(CamsAppointmentRepository appointmentRepository, CamsSlotRepository slotRepository,
            CamsUserRepository userRepository, CamsLeaveRepository leaveRepository,
            CamsAuditRepository auditRepository, CamsCatalogService catalogService, CamsClock clock) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.leaveRepository = leaveRepository;
        this.auditRepository = auditRepository;
        this.catalogService = catalogService;
        this.clock = clock;
    }

    private int setting(String key) {
        return catalogService.settingsMap().get(key);
    }

    /** Mirrors Application.appointment(): loads a row and checks the caller may see it. */
    public CamsAppointment getForUser(Long id, CamsUser user) {
        CamsAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new CamsApiException(404, "Không tìm thấy lịch hẹn."));
        CamsSlot slot = slotRepository.findById(appointment.getSlotId())
                .orElseThrow(() -> new CamsApiException(404, "Không tìm thấy lịch hẹn."));
        if (user.getRole() == CamsRole.patient) {
            require(appointment.getPatientId().equals(user.getId()), "Không có quyền xem lịch này.", 403, "forbidden");
        }
        if (user.getRole() == CamsRole.doctor) {
            require(slot.getDoctorId().equals(user.getId()), "Không có quyền xem lịch này.", 403, "forbidden");
        }
        return appointment;
    }

    public Map<String, Object> toJson(CamsAppointment appointment) {
        return toJsonBatch(List.of(appointment)).get(0);
    }

    /** Bulk-assembles the same joined shape app.py's SQL JOIN produces, in one round trip per table. */
    public List<Map<String, Object>> toJsonBatch(List<CamsAppointment> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Long, CamsSlot> slots = slotRepository
                .findAllById(appointments.stream().map(CamsAppointment::getSlotId).distinct().toList())
                .stream().collect(LinkedHashMap::new, (m, s) -> m.put(s.getId(), s), Map::putAll);
        Set<Long> userIds = new java.util.HashSet<>();
        appointments.forEach(a -> userIds.add(a.getPatientId()));
        slots.values().forEach(s -> userIds.add(s.getDoctorId()));
        Map<Long, CamsUser> users = userRepository.findAllById(userIds).stream()
                .collect(LinkedHashMap::new, (m, u) -> m.put(u.getId(), u), Map::putAll);

        return appointments.stream().map(a -> {
            CamsSlot slot = slots.get(a.getSlotId());
            CamsUser patient = users.get(a.getPatientId());
            CamsUser doctor = slot == null ? null : users.get(slot.getDoctorId());
            boolean onLeave = slot != null && leaveRepository.existsByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(
                    slot.getDoctorId(), slot.getEndsAt(), slot.getStartsAt());

            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", a.getId());
            json.put("patient_id", a.getPatientId());
            json.put("slot_id", a.getSlotId());
            json.put("doctor_id", slot == null ? null : slot.getDoctorId());
            json.put("starts_at", slot == null ? null : CamsTime.stamp(slot.getStartsAt()));
            json.put("ends_at", slot == null ? null : CamsTime.stamp(slot.getEndsAt()));
            json.put("doctor_name", doctor == null ? null : doctor.getName());
            json.put("patient_name", !a.getPatientNameOverride().isEmpty() ? a.getPatientNameOverride()
                    : patient == null ? null : patient.getName());
            json.put("patient_phone", !a.getPatientPhoneOverride().isEmpty() ? a.getPatientPhoneOverride()
                    : patient == null ? null : patient.getPhone());
            json.put("status", a.getStatus().name());
            json.put("channel", a.getChannel().name());
            json.put("reason", a.getReason());
            json.put("reschedule_count", a.getRescheduleCount());
            json.put("created_by", a.getCreatedBy());
            json.put("created_at", CamsTime.stamp(a.getCreatedAt()));
            json.put("doctor_on_leave", onLeave ? 1 : 0);
            json.put("booking_details", a.getBookingDetails());
            return json;
        }).toList();
    }

    public List<Map<String, Object>> list(CamsUser user) {
        List<CamsAppointment> rows;
        if (user.getRole() == CamsRole.patient) {
            rows = appointmentRepository.findByPatientId(user.getId());
        } else if (user.getRole() == CamsRole.doctor) {
            List<Long> slotIds = slotRepository.findByDoctorId(user.getId()).stream().map(CamsSlot::getId).toList();
            rows = appointmentRepository.findBySlotIdIn(slotIds);
        } else {
            rows = appointmentRepository.findAll();
        }
        List<Map<String, Object>> json = toJsonBatch(rows);
        return json.stream()
                .sorted(Comparator.<Map<String, Object>, String>comparing(m -> (String) m.get("starts_at"),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(m -> (Long) m.get("id"), Comparator.reverseOrder()))
                .toList();
    }

    @Transactional
    public Map<String, Object> create(CamsUser actor, Map<String, Object> data) {
        long slotId = integer(data.get("slot_id"), "Khung giờ");
        String reason = text(data.get("reason"), "Lý do khám", 500);
        CamsSlot slot = catalogService.validateSlot(slotId, null);

        Long patientId;
        AppointmentStatus status;
        BookingChannel channel;
        if (actor.getRole() == CamsRole.patient) {
            patientId = actor.getId();
            status = AppointmentStatus.pending;
            channel = BookingChannel.online;
            long activeCount = appointmentRepository.countByPatientIdAndStatusIn(patientId, ACTIVE_STATUSES);
            require(activeCount < setting("max_active"), "Đã đạt giới hạn lịch hẹn chưa khám.", 409);
        } else if (actor.getRole() == CamsRole.receptionist) {
            String guestName = text(data.get("patient_name"), "Họ tên bệnh nhân", 100);
            String guestPhone = text(data.get("patient_phone"), "Điện thoại bệnh nhân", 20, 8);
            require(PHONE.matcher(guestPhone).matches(), "Số điện thoại không hợp lệ.");
            String existingEmail = text(data.getOrDefault("patient_email", ""), "Email bệnh nhân", 150, 0)
                    .toLowerCase();
            if (!existingEmail.isEmpty()) {
                CamsUser existing = userRepository.findByEmailAndRoleAndActiveTrue(existingEmail, CamsRole.patient)
                        .orElseThrow(() -> new CamsApiException(404, "Không tìm thấy tài khoản bệnh nhân theo email."));
                patientId = existing.getId();
            } else {
                CamsUser guest = new CamsUser();
                guest.setName(guestName);
                guest.setRole(CamsRole.patient);
                guest.setPhone(guestPhone);
                patientId = userRepository.save(guest).getId();
            }
            status = AppointmentStatus.confirmed;
            channel = BookingChannel.staff;
        } else {
            throw new CamsApiException(403, "Bạn không có quyền thực hiện thao tác này.", "forbidden");
        }

        Map<String, Object> clean = cleanBookingDetails(data.get("booking_details"));
        String overrideName = "", overridePhone = "";
        if (!clean.isEmpty()) {
            Object bookingFor = clean.get("booking_for");
            require("self".equals(bookingFor) || "relative".equals(bookingFor), "Chọn người khám hợp lệ.");
            overrideName = text(clean.get("name"), "Họ tên người khám", 100);
            overridePhone = text(clean.get("phone"), "Điện thoại người khám", 20, 8);
            require(PHONE.matcher(overridePhone).matches(), "Số điện thoại không hợp lệ.");
            Object gender = clean.getOrDefault("gender", "");
            require(Set.of("", "male", "female", "other").contains(gender), "Giới tính không hợp lệ.");
            require("at_clinic".equals(clean.getOrDefault("payment", "at_clinic")),
                    "Chỉ hỗ trợ thanh toán tại cơ sở.");
            if (clean.get("birth_year") != null && !String.valueOf(clean.get("birth_year")).isEmpty()) {
                long year = integer(clean.get("birth_year"), "Năm sinh");
                require(year >= 1900 && year <= clock.now().getYear(), "Năm sinh không hợp lệ.");
            }
        }

        CamsAppointment appointment = new CamsAppointment();
        appointment.setPatientId(patientId);
        appointment.setSlotId(slot.getId());
        appointment.setChannel(channel);
        appointment.setStatus(status);
        appointment.setReason(reason);
        appointment.setCreatedBy(actor.getId());
        appointment.setCreatedAt(clock.now());
        appointment.setBookingDetails(clean.isEmpty() ? "" : writeJson(clean));
        appointment.setPatientNameOverride(overrideName);
        appointment.setPatientPhoneOverride(overridePhone);
        appointmentRepository.save(appointment);

        Map<String, Object> after = toJson(appointment);
        audit(appointment.getId(), actor, "create", Map.of(), after, "Tạo lịch " + channel.name());
        return after;
    }

    @Transactional
    public Map<String, Object> transition(Long id, String action, CamsUser actor, Map<String, Object> data) {
        CamsAppointment appointment = getForUser(id, actor);
        Map<String, Object> before = toJson(appointment);
        String reason;

        {
            require(actor.getRole() == CamsRole.receptionist, "Bạn không có quyền thực hiện thao tác này.", 403,
                    "forbidden");
            Map<String, AppointmentStatus[]> transitions = Map.of(
                    "approve", new AppointmentStatus[] { AppointmentStatus.pending, AppointmentStatus.confirmed },
                    "reject", new AppointmentStatus[] { AppointmentStatus.pending, AppointmentStatus.cancelled });
            AppointmentStatus[] pair = transitions.get(action);
            require(pair != null, "Không tìm thấy chức năng.", 404, "not_found");
            require(appointment.getStatus() == pair[0], "Chuyển trạng thái không hợp lệ.", 409, "invalid_transition");
            reason = action.equals("reject") ? text(data.get("reason"), "Lý do từ chối", 300)
                    : "Chuyển trạng thái " + pair[1].name();
            appointment.setStatus(pair[1]);
        }

        appointmentRepository.save(appointment);
        Map<String, Object> after = toJson(appointment);
        audit(appointment.getId(), actor, action, before, after, reason);
        return after;
    }

    public List<Map<String, Object>> auditTrail(Long appointmentId, CamsUser actor) {
        getForUser(appointmentId, actor); // ownership check, result unused
        return auditRepository.findByAppointmentIdOrderByIdAsc(appointmentId).stream().map(row -> {
            CamsUser user = userRepository.findById(row.getActorId()).orElse(null);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", row.getId());
            json.put("appointment_id", row.getAppointmentId());
            json.put("actor_id", row.getActorId());
            json.put("actor_name", user == null ? null : user.getName());
            json.put("action", row.getAction());
            json.put("before_json", row.getBeforeJson());
            json.put("after_json", row.getAfterJson());
            json.put("reason", row.getReason());
            json.put("created_at", CamsTime.stamp(row.getCreatedAt()));
            return json;
        }).toList();
    }

    private void audit(Long appointmentId, CamsUser actor, String action, Map<String, Object> before,
            Map<String, Object> after, String reason) {
        CamsAudit entry = new CamsAudit();
        entry.setAppointmentId(appointmentId);
        entry.setActorId(actor.getId());
        entry.setAction(action);
        entry.setBeforeJson(writeJson(before));
        entry.setAfterJson(writeJson(after));
        entry.setReason(reason);
        entry.setCreatedAt(clock.now());
        auditRepository.save(entry);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cleanBookingDetails(Object raw) {
        if (raw == null) {
            return Map.of();
        }
        require(raw instanceof Map, "Thông tin người khám không hợp lệ.");
        Map<String, Object> details = (Map<String, Object>) raw;
        require(BOOKING_DETAIL_KEYS.containsAll(details.keySet()), "Trường thông tin người khám không được hỗ trợ.");
        Map<String, Object> clean = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            int max = Set.of("address", "extra_note").contains(entry.getKey()) ? 500 : 100;
            clean.put(entry.getKey(), text(entry.getValue(), entry.getKey(), max, 0));
        }
        return clean;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new CamsApiException(500, "Lỗi máy chủ.", "internal_error");
        }
    }
}
