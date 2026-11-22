package com.example.cams.service;

import com.example.cams.dto.request.leave.CreateLeaveRequest;
import com.example.cams.dto.response.leave.AffectedAppointment;
import com.example.cams.dto.response.leave.CreateLeaveResponse;
import com.example.cams.dto.response.leave.LeaveInfo;
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
import static com.example.cams.util.CamsValidation.requiredInteger;
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

    public List<LeaveInfo> list(CamsUser user) {
        List<CamsLeave> rows = user.getRole() == CamsRole.doctor
                ? leaveRepository.findByDoctorId(user.getId())
                : leaveRepository.findAll();
        return rows.stream().map(l -> {
            CamsUser doctor = userRepository.findById(l.getDoctorId()).orElse(null);
            return new LeaveInfo(
                    l.getId(),
                    l.getDoctorId(),
                    doctor == null ? null : doctor.getName(),
                    CamsTime.stamp(l.getStartsAt()),
                    CamsTime.stamp(l.getEndsAt()),
                    l.getReason(),
                    l.getCreatedBy());
        }).toList();
    }

    @Transactional
    public CreateLeaveResponse create(CamsUser actor, CreateLeaveRequest data) {
        long doctorId = actor.getRole() == CamsRole.doctor ? actor.getId() : requiredInteger(data.doctorId(), "Bác sĩ");
        OffsetDateTime start = CamsTime.parseDatetime(data.startsAt());
        OffsetDateTime end = CamsTime.parseDatetime(data.endsAt());
        require(start.isBefore(end), "Ngày kết thúc phải sau ngày bắt đầu.");
        require(end.isAfter(clock.now()), "Khoảng nghỉ đã kết thúc.");
        String reason = text(data.reason(), "Lý do nghỉ", 300);

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

        List<AffectedAppointment> affectedAppointments = affected.stream().map(a -> {
            CamsSlot slot = slotById.get(a.getSlotId());
            CamsUser patient = userRepository.findById(a.getPatientId()).orElse(null);
            return new AffectedAppointment(
                    a.getId(),
                    a.getStatus().name(),
                    slot == null ? null : CamsTime.stamp(slot.getStartsAt()),
                    !a.getPatientNameOverride().isEmpty() ? a.getPatientNameOverride()
                    : patient == null ? null : patient.getName(),
                    !a.getPatientPhoneOverride().isEmpty() ? a.getPatientPhoneOverride()
                    : patient == null ? null : patient.getPhone());
        }).toList();

        return new CreateLeaveResponse(
                leave.getId(),
                affectedAppointments,
                "Đã chặn đặt mới. Lễ tân cần liên hệ các lịch bị ảnh hưởng.");
    }
}
