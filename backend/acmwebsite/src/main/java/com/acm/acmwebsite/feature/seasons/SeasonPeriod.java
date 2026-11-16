package com.acm.acmwebsite.feature.seasons;

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

// The chapter's calendar: seasons and their quarters (date ranges the super admin sets in Season settings)
// and calendar months (created on first use). Any feature can work per period; badges are the first user.
@Entity
@Table(name = "season_periods",
        uniqueConstraints = @UniqueConstraint(name = "uk_season_periods_type_start", columnNames = {"type", "start_date"}))
@Getter
@Setter
@NoArgsConstructor
public class SeasonPeriod {

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

    // Inclusive. Null while the season or quarter is ongoing and its end isn't known yet;
    // it is set when the next one is added, or by hand.
    @Column(name = "end_date")
    private LocalDate endDate;

    // The season this period belongs to, by its year (as in SystemSettings and Past boards).
    // A month outside every defined season has none.
    private Integer season;

    public SeasonPeriod(PeriodType type, String name, LocalDate startDate, LocalDate endDate, Integer season) {
        this.type = type;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.season = season;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }

    // The last day counted so far: the end date, or `today` while the period is ongoing.
    public LocalDate lastDayAsOf(LocalDate today) {
        return endDate != null ? endDate : today;
    }

    // "Nov 2026", "Q2 · 2026", "Season 2026".
    public String label() {
        return switch (type) {
            case MONTH -> startDate.format(MONTH_LABEL);
            case QUARTER -> season == null ? name : name + " · " + seasonLabel(season);
            case SEASON -> "Season " + seasonLabel(season);
        };
    }

    public static String seasonLabel(int season) {
        return String.valueOf(season);
    }
}
