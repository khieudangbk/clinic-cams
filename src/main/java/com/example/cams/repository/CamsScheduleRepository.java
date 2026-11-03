package com.example.cams.repository;

import com.example.cams.entity.CamsSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CamsScheduleRepository extends JpaRepository<CamsSchedule, Long> {
    Optional<CamsSchedule> findByDoctorId(Long doctorId);
}
