package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** One row per email attempt (SC7): kind confirm|reminder, status sent|skipped|failed|disabled. */
@Entity
@Getter
@Setter
@Table(name = "cams_email_log")
public class CamsEmailLog {

    public static final String KIND_CONFIRM = "confirm";
    public static final String KIND_REMINDER = "reminder";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_SKIPPED = "skipped";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DISABLED = "disabled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(nullable = false, length = 20)
    private String kind;

    @Column(nullable = false, length = 150)
    private String recipient = "";

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 500)
    private String error = "";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
