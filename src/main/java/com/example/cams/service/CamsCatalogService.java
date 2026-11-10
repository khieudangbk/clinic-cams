package com.example.cams.service;

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

    public List<CamsUser> activeDoctors() {
        return userRepository.findByRoleAndActiveTrueOrderByNameAsc(CamsRole.doctor);
    }

    private Map<String, Object> doctorJson(CamsUser doctor, Map<Long, String> specialtyNames) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", doctor.getId());
        json.put("name", doctor.getName());
        json.put("specialty_id", doctor.getSpecialtyId());
        json.put("specialty_name", specialtyNames.get(doctor.getSpecialtyId()));
        return json;
    }

    public Map<String, Object> publicCatalog() {
        List<CamsSpecialty> specialties = specialtyRepository.findAllByOrderByNameAsc();
        Map<Long, String> names = specialties.stream()
                .collect(Collectors.toMap(CamsSpecialty::getId, CamsSpecialty::getName));
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("specialties", specialties.stream().map(this::specialtyJson).toList());
        json.put("doctors", activeDoctors().stream().map(d -> doctorJson(d, names)).toList());
        json.put("now", CamsTime.stamp(clock.now()));
        json.put("clock_mode", clock.isDemo() ? "demo" : "real");
        return json;
    }

    public Map<String, Object> fullCatalog() {
        List<CamsSpecialty> specialties = specialtyRepository.findAllByOrderByNameAsc();
        Map<Long, String> names = specialties.stream()
                .collect(Collectors.toMap(CamsSpecialty::getId, CamsSpecialty::getName));
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("specialties", specialties.stream().map(this::specialtyJson).toList());
        json.put("doctors", activeDoctors().stream().map(d -> doctorJson(d, names)).toList());
        json.put("schedules", scheduleRepository.findAll().stream().map(this::scheduleJson).toList());
        return json;
    }

    private Map<String, Object> specialtyJson(CamsSpecialty specialty) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", specialty.getId());
        json.put("name", specialty.getName());
        return json;
    }

    public Map<String, Object> scheduleJson(CamsSchedule schedule) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("doctor_id", schedule.getDoctorId());
        json.put("weekdays", schedule.getWeekdays());
        json.put("start_time", schedule.getStartTime());
        json.put("end_time", schedule.getEndTime());
        return json;
    }

}
