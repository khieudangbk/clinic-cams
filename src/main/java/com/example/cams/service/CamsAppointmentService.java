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
            json.put("patient_name", patient == null ? null : patient.getName());
            json.put("patient_phone", patient == null ? null : patient.getPhone());
            json.put("status", a.getStatus().name());
            json.put("channel", a.getChannel().name());
            json.put("reason", a.getReason());
            json.put("reschedule_count", a.getRescheduleCount());
            json.put("created_by", a.getCreatedBy());
            json.put("created_at", CamsTime.stamp(a.getCreatedAt()));
            json.put("doctor_on_leave", onLeave ? 1 : 0);
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

}
