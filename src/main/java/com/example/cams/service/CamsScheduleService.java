package com.example.cams.service;

import com.example.cams.dto.request.schedule.SaveScheduleRequest;
import com.example.cams.entity.CamsSchedule;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsScheduleRepository;
import com.example.cams.repository.CamsUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.example.cams.util.CamsValidation.requiredInteger;
import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Doctor weekly working-hours template — mirrors app.py's POST /api/schedules. */
@Service
public class CamsScheduleService {

    private final CamsScheduleRepository scheduleRepository;
    private final CamsUserRepository userRepository;

    public CamsScheduleService(CamsScheduleRepository scheduleRepository, CamsUserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void save(SaveScheduleRequest data) {
        long doctorId = requiredInteger(data.doctorId(), "Bác sĩ");
        List<Integer> weekdaysList = data.weekdays();
        require(weekdaysList != null && !weekdaysList.isEmpty(), "Chọn thứ Hai đến thứ Bảy.");
        Set<Integer> weekdays = new TreeSet<>();
        for (Integer day : weekdaysList) {
            require(day != null && day >= 0 && day <= 5, "Chọn thứ Hai đến thứ Bảy.");
            weekdays.add(day);
        }
        String start = text(data.startTime(), "Giờ bắt đầu", 5);
        String end = text(data.endTime(), "Giờ kết thúc", 5);
        try {
            LocalTime.parse(start);
            LocalTime.parse(end);
        } catch (Exception e) {
            require(false, "Giờ làm việc không hợp lệ.");
        }
        require(start.matches("\\d{2}:\\d{2}") && end.matches("\\d{2}:\\d{2}")
                && "07:00".compareTo(start) <= 0 && start.compareTo(end) < 0 && end.compareTo("17:00") <= 0,
                "Giờ làm việc trong 07:00–17:00.");

        require(userRepository.existsByIdAndRoleAndActiveTrue(doctorId, CamsRole.doctor), "Bác sĩ không tồn tại.");

        CamsSchedule schedule = scheduleRepository.findByDoctorId(doctorId).orElseGet(() -> {
            CamsSchedule fresh = new CamsSchedule();
            fresh.setDoctorId(doctorId);
            return fresh;
        });
        StringBuilder csv = new StringBuilder();
        for (int day : weekdays) {
            if (csv.length() > 0) {
                csv.append(',');
            }
            csv.append(day);
        }
        schedule.setWeekdays(csv.toString());
        schedule.setStartTime(start);
        schedule.setEndTime(end);
        scheduleRepository.save(schedule);
    }
}
