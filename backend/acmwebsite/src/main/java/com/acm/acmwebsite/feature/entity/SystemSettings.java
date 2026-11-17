package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

// Site-wide settings stored as a single row (id = 1)
@Entity
@DynamicUpdate
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    // Master email switch: when false, the site sends no emails except password resets
    @Column(name = "emails_enabled", nullable = false)
    private boolean emailsEnabled = true;

    // The current season used to be stored here (current_season). It now comes from Season settings
    // (season_periods); see CurrentSeasonService, which reads the old value once to create the first season.
}
