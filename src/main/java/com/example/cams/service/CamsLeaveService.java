package com.example.cams.service;

import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.CamsLeave;
import com.example.cams.entity.CamsSlot;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsAppointmentRepository;
import com.example.cams.repository.CamsLeaveRepository;
import com.example.cams.repository.CamsSlotRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.cams.service.CamsCatalogService.ACTIVE_STATUSES;
import static com.example.cams.util.CamsValidation.integer;
import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Doctor leave registration and the appointments it affects — mirrors app.py's /api/leaves. */
@Service
public class CamsLeaveService {

    private final CamsLeaveRepository leaveRepository;
    private final CamsUserRepository userRepository;
    private final CamsSlotRepository slotRepository;
    private final CamsAppointmentRepository appointmentRepository;
    private final CamsClock clock;

    public CamsLeaveService(CamsLeaveRepository leaveRepository, CamsUserRepository userRepository,
            CamsSlotRepository slotRepository, CamsAppointmentRepository appointmentRepository, CamsClock clock) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public List<Map<String, Object>> list(CamsUser user) {
        List<CamsLeave> rows = user.getRole() == CamsRole.doctor
                ? leaveRepository.findByDoctorId(user.getId())
                : leaveRepository.findAll();
        return rows.stream().map(l -> {
            CamsUser doctor = userRepository.findById(l.getDoctorId()).orElse(null);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", l.getId());
            json.put("doctor_id", l.getDoctorId());
            json.put("doctor_name", doctor == null ? null : doctor.getName());
            json.put("starts_at", CamsTime.stamp(l.getStartsAt()));
            json.put("ends_at", CamsTime.stamp(l.getEndsAt()));
            json.put("reason", l.getReason());
            json.put("created_by", l.getCreatedBy());
            return json;
        }).toList();
    }

    @Transactional
    public Map<String, Object> create(CamsUser actor, Map<String, Object> data) {
        long doctorId = actor.getRole() == CamsRole.doctor ? actor.getId() : integer(data.get("doctor_id"), "Bác sĩ");
        OffsetDateTime start = CamsTime.parseDatetime(data.get("starts_at"));
        OffsetDateTime end = CamsTime.parseDatetime(data.get("ends_at"));
        require(start.isBefore(end), "Ngày kết thúc phải sau ngày bắt đầu.");
        require(end.isAfter(clock.now()), "Khoảng nghỉ đã kết thúc.");
        String reason = text(data.get("reason"), "Lý do nghỉ", 300);

        require(userRepository.existsByIdAndRoleAndActiveTrue(doctorId, CamsRole.doctor), "Bác sĩ không tồn tại.");

        CamsLeave leave = new CamsLeave();
        leave.setDoctorId(doctorId);
        leave.setStartsAt(start);
        leave.setEndsAt(end);
        leave.setReason(reason);
        leave.setCreatedBy(actor.getId());
        leaveRepository.save(leave);

        List<CamsSlot> overlapping = slotRepository.findByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(doctorId,
                end, start);
        List<Long> slotIds = overlapping.stream().map(CamsSlot::getId).toList();
        List<CamsAppointment> affected = slotIds.isEmpty() ? List.of()
                : appointmentRepository.findBySlotIdIn(slotIds).stream()
                        .filter(a -> ACTIVE_STATUSES.contains(a.getStatus()))
                        .toList();
        Map<Long, CamsSlot> slotById = overlapping.stream()
                .collect(LinkedHashMap::new, (m, s) -> m.put(s.getId(), s), Map::putAll);

        List<Map<String, Object>> affectedJson = affected.stream().map(a -> {
            CamsSlot slot = slotById.get(a.getSlotId());
            CamsUser patient = userRepository.findById(a.getPatientId()).orElse(null);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", a.getId());
            json.put("status", a.getStatus().name());
            json.put("starts_at", slot == null ? null : CamsTime.stamp(slot.getStartsAt()));
            json.put("patient_name", !a.getPatientNameOverride().isEmpty() ? a.getPatientNameOverride()
                    : patient == null ? null : patient.getName());
            json.put("patient_phone", !a.getPatientPhoneOverride().isEmpty() ? a.getPatientPhoneOverride()
                    : patient == null ? null : patient.getPhone());
            return json;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", leave.getId());
        result.put("affected", affectedJson);
        result.put("message", "Đã chặn đặt mới. Lễ tân cần liên hệ các lịch bị ảnh hưởng.");
        return result;
    }
}
