package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Opaque server-side session: cookie holds the raw token, only its SHA-256 hash is stored. */
@Entity
@Getter
@Setter
@Table(name = "cams_sessions")
public class CamsSession {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String csrf;

    @Column(nullable = false)
    private Instant expires;
}
