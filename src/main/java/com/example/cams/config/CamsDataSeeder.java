package com.example.cams.config;

import com.example.cams.entity.CamsSchedule;
import com.example.cams.entity.CamsSetting;
import com.example.cams.entity.CamsSpecialty;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsScheduleRepository;
import com.example.cams.repository.CamsSettingRepository;
import com.example.cams.repository.CamsSpecialtyRepository;
import com.example.cams.repository.CamsUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Order(100)
public class CamsDataSeeder implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "Demo@12345";
    private static final Map<String, Integer> DEFAULT_SETTINGS = Map.of(
            "slot_minutes", 30, "cutoff_hours", 2, "max_reschedules", 2, "advance_days", 30, "max_active", 3);

    private final CamsSpecialtyRepository specialtyRepository;
    private final CamsUserRepository userRepository;
    private final CamsScheduleRepository scheduleRepository;
    private final CamsSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public CamsDataSeeder(CamsSpecialtyRepository specialtyRepository, CamsUserRepository userRepository,
            CamsScheduleRepository scheduleRepository, CamsSettingRepository settingRepository,
            PasswordEncoder passwordEncoder) {
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Map.Entry<String, Integer> entry : DEFAULT_SETTINGS.entrySet()) {
            if (settingRepository.findById(entry.getKey()).isEmpty()) {
                settingRepository.save(new CamsSetting(entry.getKey(), entry.getValue()));
            }
        }
        if (userRepository.count() > 0) {
            return;
        }
        List<CamsSpecialty> specialties = List.of("Nội tổng quát", "Tim mạch", "Da liễu").stream()
                .map(name -> {
                    CamsSpecialty specialty = new CamsSpecialty();
                    specialty.setName(name);
                    return specialtyRepository.save(specialty);
                }).toList();

        String hashed = passwordEncoder.encode(DEMO_PASSWORD);
        seedUser("Quản lý Demo", "admin@cams.local", CamsRole.manager, hashed, null, "0900000000");
        seedUser("Lễ tân Demo", "staff@cams.local", CamsRole.receptionist, hashed, null, "0900000000");
        seedUser("Bệnh nhân Demo", "patient@cams.local", CamsRole.patient, hashed, null, "0900000000");

        for (int i = 1; i <= 10; i++) {
            Long specialtyId = specialties.get((i - 1) % specialties.size()).getId();
            CamsUser doctor = seedUser(String.format("Bác sĩ Demo %02d", i), String.format("doctor%02d@cams.local", i),
                    CamsRole.doctor, hashed, specialtyId, "");
            CamsSchedule schedule = new CamsSchedule();
            schedule.setDoctorId(doctor.getId());
            scheduleRepository.save(schedule);
        }
    }

    private CamsUser seedUser(String name, String email, CamsRole role, String passwordHash, Long specialtyId,
            String phone) {
        CamsUser user = new CamsUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setPhone(phone);
        user.setSpecialtyId(specialtyId);
        return userRepository.save(user);
    }
}
