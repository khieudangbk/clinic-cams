package com.example.cams.repository;

import com.example.cams.entity.CamsEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CamsEmailLogRepository extends JpaRepository<CamsEmailLog, Long> {

    List<CamsEmailLog> findByAppointmentIdOrderByIdAsc(Long appointmentId);

    List<CamsEmailLog> findTop200ByOrderByIdDesc();
}
