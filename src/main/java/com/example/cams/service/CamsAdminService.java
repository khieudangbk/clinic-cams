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

}
