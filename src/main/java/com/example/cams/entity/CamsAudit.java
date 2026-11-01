package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "cams_audit")
public class CamsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(name = "before_json", nullable = false, columnDefinition = "text")
    private String beforeJson;

    @Column(name = "after_json", nullable = false, columnDefinition = "text")
    private String afterJson;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
