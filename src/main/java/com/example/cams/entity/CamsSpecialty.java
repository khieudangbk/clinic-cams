package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cams_specialties")
public class CamsSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // false = stopped: hidden from the catalog and not bookable; existing appointments are kept.
    @Column(nullable = false)
    private boolean active = true;
}
