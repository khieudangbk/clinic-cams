package com.example.cams.repository;

import com.example.cams.entity.CamsAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CamsAuditRepository extends JpaRepository<CamsAudit, Long> {
    List<CamsAudit> findByAppointmentIdOrderByIdAsc(Long appointmentId);
}
