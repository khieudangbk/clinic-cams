package com.example.cams.service;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.cams.util.CamsValidation.integer;
import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Staff/specialty/settings administration — mirrors app.py's /api/users, /api/specialties, /api/settings. */
@Service
public class CamsAdminService {

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

    public static Map<String, Object> publicUser(CamsUser user) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", user.getId());
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("phone", user.getPhone());
        json.put("role", user.getRole().name());
        json.put("specialty_id", user.getSpecialtyId());
        json.put("active", user.isActive() ? 1 : 0);
        return json;
    }

    public List<Map<String, Object>> listStaffUsers() {
        return userRepository
                .findByRoleInOrderByIdAsc(List.of(CamsRole.doctor, CamsRole.receptionist, CamsRole.manager))
                .stream().map(CamsAdminService::publicUser).toList();
    }

    @Transactional
    public Long createStaffUser(Map<String, Object> data) {
        Object role = data.get("role");
        require("doctor".equals(role) || "receptionist".equals(role), "Chỉ tạo bác sĩ hoặc nhân viên.");
        String name = text(data.get("name"), "Họ tên", 100);
        String email = text(data.get("email"), "Email", 150).toLowerCase();
        require(email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Email không hợp lệ.");
        String hashed = passwordEncoder.encode(text(data.get("password"), "Mật khẩu", 128, 10));
        Long specialtyId = "doctor".equals(role) ? integer(data.get("specialty_id"), "Chuyên khoa") : null;
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
    public void updateStaffUser(Long id, Map<String, Object> data) {
        CamsUser user = userRepository.findById(id)
                .filter(u -> u.getRole() == CamsRole.doctor || u.getRole() == CamsRole.receptionist)
                .orElseThrow(() -> new CamsApiException(404, "Tài khoản không thuộc phạm vi quản trị."));

        String name = data.containsKey("name") ? text(data.get("name"), "Họ tên", 100) : user.getName();
        Object activeRaw = data.getOrDefault("active", user.isActive() ? 1 : 0);
        require(Integer.valueOf(1).equals(activeRaw) || Integer.valueOf(0).equals(activeRaw)
                || Boolean.TRUE.equals(activeRaw) || Boolean.FALSE.equals(activeRaw), "Trạng thái tài khoản không hợp lệ.");
        boolean active = Integer.valueOf(1).equals(activeRaw) || Boolean.TRUE.equals(activeRaw);

        Long specialtyId = data.containsKey("specialty_id")
                ? (data.get("specialty_id") == null ? null : integer(data.get("specialty_id"), "Chuyên khoa"))
                : user.getSpecialtyId();
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

}
