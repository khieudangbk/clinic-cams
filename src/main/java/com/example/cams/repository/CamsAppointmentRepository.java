package com.example.cams.repository;

import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import java.util.Collection;
import java.util.List;

public interface CamsAppointmentRepository extends JpaRepository<CamsAppointment, Long> {

    boolean existsBySlotIdInAndStatusInAndIdNot(Collection<Long> slotIds, Collection<AppointmentStatus> statuses,
            Long excludeId);

    List<CamsAppointment> findBySlotIdIn(Collection<Long> slotIds);

    List<CamsAppointment> findByPatientId(Long patientId);

    long countByPatientIdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);

    /** Confirmed, not yet reminded appointments whose slot starts in (from, to]. */
    @Query("select a from CamsAppointment a, CamsSlot s where a.slotId = s.id and a.status = :status "
            + "and a.reminderSentAt is null and s.startsAt > :from and s.startsAt <= :to order by s.startsAt")
    List<CamsAppointment> findDueForReminder(@Param("status") AppointmentStatus status,
            @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Atomically takes the reminder for one appointment; returns 0 if someone else already did. */
    @Transactional
    @Modifying
    @Query("update CamsAppointment a set a.reminderSentAt = :at where a.id = :id and a.reminderSentAt is null")
    int claimReminder(@Param("id") Long id, @Param("at") OffsetDateTime at);

    /** Gives the reminder back (send failed) so the next run retries it. */
    @Transactional
    @Modifying
    @Query("update CamsAppointment a set a.reminderSentAt = null where a.id = :id")
    int releaseReminder(@Param("id") Long id);
}
