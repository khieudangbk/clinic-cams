package com.example.cams.service;

import com.example.cams.dto.request.admin.CreateStaffUserRequest;
import com.example.cams.dto.request.admin.UpdateSettingsRequest;
import com.example.cams.dto.request.admin.UpdateStaffUserRequest;
import com.example.cams.dto.request.admin.UpsertSpecialtyRequest;
import com.example.cams.dto.response.account.UserInfo;
import com.example.cams.entity.CamsSchedule;
import com.example.cams.entity.CamsSetting;
import com.example.cams.entity.CamsSpecialty;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsScheduleRepository;
import com.example.cams.repository.CamsSessionRepository;
import com.example.cams.repository.CamsSettingRepository;
import com.example.cams.repository.CamsSpecialtyRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.exception.CamsApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.example.cams.util.CamsValidation.requiredInteger;
import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Staff/specialty/settings administration — mirrors app.py's /api/users, /api/specialties, /api/settings. */
@Service
public class CamsAdminService {

    private static final Map<String, int[]> SETTING_BOUNDS = Map.of(
            "slot_minutes", new int[] { 5, 120 },
            "cutoff_hours", new int[] { 0, 72 },
            "max_reschedules", new int[] { 0, 10 },
            "reminder_hours", new int[] { 1, 72 });

    private final CamsUserRepository userRepository;
    private final CamsSpecialtyRepository specialtyRepository;
    private final CamsScheduleRepository scheduleRepository;
    private final CamsSettingRepository settingRepository;
    private final CamsSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public CamsAdminService(CamsUserRepository userRepository, CamsSpecialtyRepository specialtyRepository,
            CamsScheduleRepository scheduleRepository, CamsSettingRepository settingRepository,
            CamsSessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.scheduleRepository = scheduleRepository;
        this.settingRepository = settingRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static UserInfo publicUser(CamsUser user) {
        return new UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getSpecialtyId(),
                user.isActive() ? 1 : 0);
    }

    public List<UserInfo> listStaffUsers() {
        return userRepository
                .findByRoleInOrderByIdAsc(List.of(CamsRole.doctor, CamsRole.receptionist, CamsRole.manager))
                .stream().map(CamsAdminService::publicUser).toList();
    }

    @Transactional
    public Long createStaffUser(CreateStaffUserRequest data) {
        String role = data.role();
        require("doctor".equals(role) || "receptionist".equals(role), "Chỉ tạo bác sĩ hoặc nhân viên.");
        String name = text(data.name(), "Họ tên", 100);
        String email = text(data.email(), "Email", 150).toLowerCase();
        require(email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Email không hợp lệ.");
        String hashed = passwordEncoder.encode(text(data.password(), "Mật khẩu", 128, 10));
        Long specialtyId = "doctor".equals(role) ? requiredInteger(data.specialtyId(), "Chuyên khoa") : null;
        if (specialtyId != null) {
            require(specialtyRepository.findById(specialtyId).isPresent(), "Chuyên khoa không tồn tại.");
        }

        CamsUser user = new CamsUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(hashed);
        user.setRole("doctor".equals(role) ? CamsRole.doctor : CamsRole.receptionist);
        user.setSpecialtyId(specialtyId);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new CamsApiException(409, "Email đã tồn tại.", "email_exists");
        }
        if (user.getRole() == CamsRole.doctor) {
            CamsSchedule schedule = new CamsSchedule();
            schedule.setDoctorId(user.getId());
            scheduleRepository.save(schedule);
        }
        return user.getId();
    }

    @Transactional
    public void updateStaffUser(Long id, UpdateStaffUserRequest data) {
        CamsUser user = userRepository.findById(id)
                .filter(u -> u.getRole() == CamsRole.doctor || u.getRole() == CamsRole.receptionist)
                .orElseThrow(() -> new CamsApiException(404, "Tài khoản không thuộc phạm vi quản trị."));

        String name = data.getName() == null ? user.getName() : text(data.getName(), "Họ tên", 100);
        boolean active = data.getActive() == null ? user.isActive() : data.getActive();
        Long specialtyId = data.isSpecialtyIdProvided() ? data.getSpecialtyId() : user.getSpecialtyId();
        if (user.getRole() == CamsRole.doctor) {
            require(specialtyId != null && specialtyRepository.findById(specialtyId).isPresent(),
                    "Chuyên khoa không tồn tại.");
        }

        user.setName(name);
        user.setActive(active);
        user.setSpecialtyId(specialtyId);
        userRepository.save(user);
        if (!active) {
            sessionRepository.deleteByUserId(id);
        }
    }

    @Transactional
    public void upsertSpecialty(UpsertSpecialtyRequest data) {
        String name = text(data.name(), "Chuyên khoa", 100);
        if (data.id() != null) {
            long id = requiredInteger(data.id(), "Chuyên khoa");
            CamsSpecialty specialty = specialtyRepository.findById(id)
                    .orElseThrow(() -> new CamsApiException(404, "Chuyên khoa không tồn tại."));
            specialty.setName(name);
            specialtyRepository.save(specialty);
        } else {
            CamsSpecialty specialty = new CamsSpecialty();
            specialty.setName(name);
            specialtyRepository.save(specialty);
        }
    }

    @Transactional
    public void updateSettings(UpdateSettingsRequest data) {
        Map<String, Long> values = data.values();
        require(!values.isEmpty() && SETTING_BOUNDS.keySet().containsAll(values.keySet()), "Tham số không được hỗ trợ.");
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            long value = requiredInteger(entry.getValue(), entry.getKey());
            int[] bounds = SETTING_BOUNDS.get(entry.getKey());
            require(value >= bounds[0] && value <= bounds[1],
                    entry.getKey() + " ngoài phạm vi " + bounds[0] + "–" + bounds[1] + ".");
            settingRepository.save(new CamsSetting(entry.getKey(), (int) value));
        }
    }
}
