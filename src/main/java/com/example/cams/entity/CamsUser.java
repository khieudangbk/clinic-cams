package com.example.cams.entity;

import com.example.cams.entity.enums.CamsRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * CAMS domain user: patient, receptionist, doctor or manager. Separate from
 * {@code com.example.cams.entity.User}, which belongs to the unrelated generic auth module
 * (its own {@code users} table, roles USER/ADMIN). Mirrors app.py's users table.
 */
@Entity
@Getter
@Setter
@Table(name = "cams_users")
public class CamsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Nullable: receptionists can book a guest patient with no account/email.
    @Column(length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CamsRole role;

    @Column(nullable = false, length = 20)
    private String phone = "";

    @Column(name = "specialty_id")
    private Long specialtyId;

    @Column(nullable = false)
    private boolean active = true;
}
