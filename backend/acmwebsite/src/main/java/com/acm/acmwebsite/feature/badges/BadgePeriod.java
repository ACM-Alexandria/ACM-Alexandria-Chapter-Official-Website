package com.acm.acmwebsite.feature.badges;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A span of time badges are awarded for. Seasons and quarters are created by admins with any dates;
// months are created on first use and always cover a whole calendar month.
@Entity
@Table(name = "badge_periods",
        uniqueConstraints = @UniqueConstraint(name = "uk_badge_periods_type_start", columnNames = {"type", "start_date"}))
@Getter
@Setter
@NoArgsConstructor
public class BadgePeriod {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PeriodType type;

    // A quarter's name as the admin typed it ("Q1"). Seasons and months leave it empty and get a generated label.
    @Column(length = 60)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Inclusive.
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Starting year of the academic season this period belongs to (2026 for 2026/27), as in SystemSettings.
    // A month outside every defined season has none.
    private Integer season;

    public BadgePeriod(PeriodType type, String name, LocalDate startDate, LocalDate endDate, Integer season) {
        this.type = type;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.season = season;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    // "Nov 2026", "Q2 · 2026/27", "Season 2026/27".
    public String label() {
        return switch (type) {
            case MONTH -> startDate.format(MONTH_LABEL);
            case QUARTER -> season == null ? name : name + " · " + seasonLabel(season);
            case SEASON -> "Season " + seasonLabel(season);
        };
    }

    public static String seasonLabel(int season) {
        return season + "/" + String.format("%02d", (season + 1) % 100);
    }
}
