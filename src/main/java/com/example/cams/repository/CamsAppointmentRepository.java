package com.example.cams.repository;

import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CamsAppointmentRepository extends JpaRepository<CamsAppointment, Long> {

    boolean existsBySlotIdInAndStatusInAndIdNot(Collection<Long> slotIds, Collection<AppointmentStatus> statuses,
            Long excludeId);

    List<CamsAppointment> findBySlotIdIn(Collection<Long> slotIds);

    List<CamsAppointment> findByPatientId(Long patientId);

    long countByPatientIdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);
}
