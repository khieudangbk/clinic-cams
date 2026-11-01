package com.example.cams.repository;

import com.example.cams.entity.CamsSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CamsSlotRepository extends JpaRepository<CamsSlot, Long> {

    Optional<CamsSlot> findByDoctorIdAndStartsAt(Long doctorId, OffsetDateTime startsAt);

    List<CamsSlot> findByDoctorIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
            Long doctorId, OffsetDateTime from, OffsetDateTime to);

    /** Slots of this doctor whose [starts_at,ends_at) interval overlaps [start,end). */
    List<CamsSlot> findByDoctorIdAndStartsAtLessThanAndEndsAtGreaterThan(
            Long doctorId, OffsetDateTime end, OffsetDateTime start);

    List<CamsSlot> findByDoctorId(Long doctorId);
}
