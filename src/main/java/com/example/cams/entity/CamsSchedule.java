package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** One weekly working-hours template per doctor. doctorId is both PK and FK. */
@Entity
@Getter
@Setter
@Table(name = "cams_schedules")
public class CamsSchedule {

    @Id
    @Column(name = "doctor_id")
    private Long doctorId;

    /** Comma-separated weekday numbers, Monday=0 .. Saturday=5 (no Sunday), e.g. "0,1,2,3,4,5". */
    @Column(nullable = false, length = 20)
    private String weekdays = "0,1,2,3,4,5";

    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime = "07:00";

    @Column(name = "end_time", nullable = false, length = 5)
    private String endTime = "17:00";
}
