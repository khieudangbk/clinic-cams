package com.example.cams.repository;

import com.example.cams.entity.CamsLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface CamsLeaveRepository extends JpaRepository<CamsLeave, Long> {

    List<CamsLeave> findByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(Long doctorId, OffsetDateTime end,
            OffsetDateTime start);
}
