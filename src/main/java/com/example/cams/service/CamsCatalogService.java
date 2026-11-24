package com.example.cams.service;

import com.example.cams.dto.response.catalog.CatalogResponse;
import com.example.cams.dto.response.catalog.DoctorInfo;
import com.example.cams.dto.response.catalog.PublicCatalogResponse;
import com.example.cams.dto.response.catalog.SpecialtyInfo;
import com.example.cams.dto.response.schedule.ScheduleInfo;
import com.example.cams.entity.CamsLeave;
import com.example.cams.entity.CamsSchedule;
import com.example.cams.entity.CamsSetting;
import com.example.cams.entity.CamsSlot;
import com.example.cams.entity.CamsSpecialty;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.AppointmentStatus;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsAppointmentRepository;
import com.example.cams.repository.CamsLeaveRepository;
import com.example.cams.repository.CamsScheduleRepository;
import com.example.cams.repository.CamsSettingRepository;
import com.example.cams.repository.CamsSlotRepository;
import com.example.cams.repository.CamsSpecialtyRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;
import com.example.cams.util.exception.CamsApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.cams.util.CamsValidation.require;

/** Catalog, working-hour schedules and slot generation/validation — mirrors app.py's Application. */
@Service
public class CamsCatalogService {

    public static final Set<AppointmentStatus> ACTIVE_STATUSES = Set.of(
            AppointmentStatus.pending, AppointmentStatus.confirmed, AppointmentStatus.arrived);

    private final CamsSpecialtyRepository specialtyRepository;
    private final CamsUserRepository userRepository;
    private final CamsScheduleRepository scheduleRepository;
    private final CamsSettingRepository settingRepository;
    private final CamsSlotRepository slotRepository;
    private final CamsLeaveRepository leaveRepository;
    private final CamsAppointmentRepository appointmentRepository;
    private final CamsClock clock;

    public CamsCatalogService(CamsSpecialtyRepository specialtyRepository, CamsUserRepository userRepository,
            CamsScheduleRepository scheduleRepository, CamsSettingRepository settingRepository,
            CamsSlotRepository slotRepository, CamsLeaveRepository leaveRepository,
            CamsAppointmentRepository appointmentRepository, CamsClock clock) {
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.settingRepository = settingRepository;
        this.slotRepository = slotRepository;
        this.leaveRepository = leaveRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public Map<String, Integer> settingsMap() {
        Map<String, Integer> settings = new LinkedHashMap<>();
        for (CamsSetting setting : settingRepository.findAll()) {
            settings.put(setting.getKey(), setting.getValue());
        }
        return settings;
    }

    private int setting(String key) {
        Integer value = settingsMap().get(key);
        require(value != null, "Thiếu cấu hình " + key + ".", 500, "internal_error");
        return value;
    }

    /** Active doctors whose specialty has not been stopped. */
    public List<CamsUser> activeDoctors() {
        Set<Long> activeSpecialties = specialtyRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(CamsSpecialty::getId).collect(Collectors.toSet());
        return userRepository.findByRoleAndActiveTrueOrderByNameAsc(CamsRole.doctor).stream()
                .filter(d -> activeSpecialties.contains(d.getSpecialtyId())).toList();
    }

    private boolean specialtyStopped(CamsUser doctor) {
        return doctor.getSpecialtyId() != null
                && specialtyRepository.findById(doctor.getSpecialtyId()).map(sp -> !sp.isActive()).orElse(false);
    }

    private DoctorInfo doctorInfo(CamsUser doctor, Map<Long, String> specialtyNames) {
        return new DoctorInfo(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialtyId(),
                specialtyNames.get(doctor.getSpecialtyId()));
    }

    public PublicCatalogResponse publicCatalog() {
        List<CamsSpecialty> specialties = specialtyRepository.findByActiveTrueOrderByNameAsc();
        Map<Long, String> names = specialties.stream()
                .collect(Collectors.toMap(CamsSpecialty::getId, CamsSpecialty::getName));
        return new PublicCatalogResponse(
                specialties.stream().map(sp -> specialtyInfo(sp, false)).toList(),
                activeDoctors().stream().map(d -> doctorInfo(d, names)).toList(),
                CamsTime.stamp(clock.now()),
                clock.isDemo() ? "demo" : "real");
    }

    /** Managers also see stopped specialties (active = 0) so they can reopen them. */
    public CatalogResponse fullCatalog(boolean includeStopped) {
        List<CamsSpecialty> specialties = includeStopped ? specialtyRepository.findAllByOrderByNameAsc()
                : specialtyRepository.findByActiveTrueOrderByNameAsc();
        Map<Long, String> names = specialties.stream()
                .collect(Collectors.toMap(CamsSpecialty::getId, CamsSpecialty::getName));
        return new CatalogResponse(
                specialties.stream().map(sp -> specialtyInfo(sp, includeStopped)).toList(),
                activeDoctors().stream().map(d -> doctorInfo(d, names)).toList(),
                scheduleRepository.findAll().stream().map(this::scheduleInfo).toList());
    }

    private SpecialtyInfo specialtyInfo(CamsSpecialty specialty, boolean withActive) {
        return new SpecialtyInfo(specialty.getId(), specialty.getName(),
                withActive ? (specialty.isActive() ? 1 : 0) : null);
    }

    private ScheduleInfo scheduleInfo(CamsSchedule schedule) {
        return new ScheduleInfo(
                schedule.getDoctorId(),
                schedule.getWeekdays(),
                schedule.getStartTime(),
                schedule.getEndTime());
    }

    private List<Integer> weekdaySet(String csv) {
        List<Integer> days = new ArrayList<>();
        for (String part : csv.split(",")) {
            if (!part.isBlank()) {
                days.add(Integer.parseInt(part.trim()));
            }
        }
        return days;
    }

    /** Lazily materialises a doctor's slots for one calendar day, mirroring generate_slots(). */
    @Transactional
    public void generateSlots(Long doctorId, LocalDate day) {
        CamsUser doctor = userRepository.findByIdAndActiveTrue(doctorId)
                .filter(u -> u.getRole() == CamsRole.doctor)
                .orElseThrow(() -> new CamsApiException(404, "Bác sĩ không tồn tại hoặc đã khóa."));
        require(!specialtyStopped(doctor), "Bác sĩ không tồn tại hoặc đã khóa.", 404);
        CamsSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new CamsApiException(400, "Bác sĩ chưa có lịch làm việc."));
        if (!weekdaySet(schedule.getWeekdays()).contains(isoWeekday(day.getDayOfWeek().getValue()))) {
            return;
        }
        OffsetDateTime dayStart = OffsetDateTime.of(day, LocalTime.MIN, CamsTime.OFFSET);
        OffsetDateTime dayEnd = dayStart.plusDays(1);
        boolean alreadyGenerated = !slotRepository
                .findByDoctorIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(doctorId, dayStart,
                        dayEnd)
                .isEmpty();
        if (alreadyGenerated) {
            return;
        }
        int stepMinutes = setting("slot_minutes");
        OffsetDateTime cursor = OffsetDateTime.of(day, LocalTime.parse(schedule.getStartTime()), CamsTime.OFFSET);
        OffsetDateTime end = OffsetDateTime.of(day, LocalTime.parse(schedule.getEndTime()), CamsTime.OFFSET);
        while (!cursor.plusMinutes(stepMinutes).isAfter(end)) {
            CamsSlot slot = new CamsSlot();
            slot.setDoctorId(doctorId);
            slot.setStartsAt(cursor);
            slot.setEndsAt(cursor.plusMinutes(stepMinutes));
            try {
                slotRepository.save(slot);
            } catch (DataIntegrityViolationException ignored) {
                // Another request generated the same slot concurrently; fine, it already exists.
            }
            cursor = cursor.plusMinutes(stepMinutes);
        }
    }

    public List<CamsSlot> listSlotsForDay(Long doctorId, LocalDate day) {
        OffsetDateTime dayStart = OffsetDateTime.of(day, LocalTime.MIN, CamsTime.OFFSET);
        OffsetDateTime dayEnd = dayStart.plusDays(1);
        return slotRepository.findByDoctorIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                doctorId, dayStart, dayEnd);
    }

    public LocalDate requireDay(String raw) {
        try {
            LocalDate day = LocalDate.parse(raw);
            OffsetDateTime today = clock.now();
            require(!day.isBefore(today.toLocalDate()) && !day.isAfter(today.toLocalDate().plusDays(30)),
                    "Ngày ngoài khoảng đặt trước.");
            return day;
        } catch (java.time.format.DateTimeParseException e) {
            throw new CamsApiException(400, "Ngày không hợp lệ.");
        }
    }

    /** Checks a slot is still bookable right now; mirrors validate_slot(). Returns the slot. */
    public CamsSlot validateSlot(Long slotId, Long excludingAppointmentId) {
        CamsSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new CamsApiException(404, "Khung giờ hoặc bác sĩ không khả dụng."));
        CamsUser doctor = userRepository.findById(slot.getDoctorId()).orElse(null);
        require(doctor != null && doctor.isActive() && !specialtyStopped(doctor),
                "Khung giờ hoặc bác sĩ không khả dụng.", 404);

        OffsetDateTime now = clock.now();
        require(slot.getStartsAt().isAfter(now), "Khung giờ đã bắt đầu.", 409, "slot_unavailable");

        int advanceDays = setting("advance_days");
        require(!slot.getStartsAt().toLocalDate().isAfter(now.toLocalDate().plusDays(advanceDays)),
                "Khung giờ vượt giới hạn đặt trước.");

        CamsSchedule schedule = scheduleRepository.findByDoctorId(slot.getDoctorId()).orElse(null);
        boolean withinSchedule = schedule != null
                && weekdaySet(schedule.getWeekdays()).contains(isoWeekday(slot.getStartsAt().getDayOfWeek().getValue()))
                && hhmm(slot.getStartsAt()).compareTo(schedule.getStartTime()) >= 0
                && hhmm(slot.getEndsAt()).compareTo(schedule.getEndTime()) <= 0;
        require(withinSchedule, "Bác sĩ không làm việc trong khung giờ này.", 409, "slot_unavailable");

        boolean onLeave = leaveRepository.existsByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(
                slot.getDoctorId(), slot.getEndsAt(), slot.getStartsAt());
        require(!onLeave, "Bác sĩ đã báo nghỉ trong khung giờ này.", 409, "doctor_on_leave");

        List<CamsSlot> overlapping = slotRepository.findByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(
                slot.getDoctorId(), slot.getEndsAt(), slot.getStartsAt());
        List<Long> overlappingIds = overlapping.stream().map(CamsSlot::getId).toList();
        boolean taken = !overlappingIds.isEmpty() && appointmentRepository.existsBySlotIdInAndStatusInAndIdNot(
                overlappingIds, ACTIVE_STATUSES, excludingAppointmentId == null ? -1L : excludingAppointmentId);
        require(!taken, "Khung giờ vừa được người khác đặt. Vui lòng chọn lại.", 409, "slot_taken");

        return slot;
    }

    private int isoWeekday(int javaDayOfWeek) {
        // java.time Monday=1..Sunday=7; app.py's Python weekday() is Monday=0..Sunday=6.
        return javaDayOfWeek - 1;
    }

    private String hhmm(OffsetDateTime value) {
        return value.withOffsetSameInstant(CamsTime.OFFSET).toLocalTime().toString().substring(0, 5);
    }
}
