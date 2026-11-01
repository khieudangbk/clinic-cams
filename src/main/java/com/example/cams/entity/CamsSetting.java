package com.example.cams.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Key/value admin-tunable settings: slot_minutes, cutoff_hours, max_reschedules, advance_days, max_active. */
@Entity
@Getter
@Setter
@Table(name = "cams_settings")
public class CamsSetting {

    @Id
    @Column(name = "setting_key", length = 50) // "key" is a reserved word in several SQL dialects
    private String key;

    @Column(name = "setting_value", nullable = false) // "value" is a reserved word in several SQL dialects
    private int value;

    public CamsSetting() {
    }

    public CamsSetting(String key, int value) {
        this.key = key;
        this.value = value;
    }
}
