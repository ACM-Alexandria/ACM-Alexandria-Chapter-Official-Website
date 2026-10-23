package com.acm.acmwebsite.feature.insights;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** The period an insights request covers. Totals ignore it; "in period" figures, charts and rankings follow it. */
public enum InsightsRange {
    D7(7), D30(30), D90(90), ALL(0);

    private static final LocalDateTime EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final int days;

    InsightsRange(int days) {
        this.days = days;
    }

    public static InsightsRange parse(String value) {
        if (value == null) return D30;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "7d" -> D7;
            case "90d" -> D90;
            case "all" -> ALL;
            default -> D30;
        };
    }

    /** Start of the first day in the period (today counts as day one). "All" starts before any data. */
    public LocalDateTime since(LocalDateTime now) {
        return days == 0 ? EPOCH : now.toLocalDate().minusDays(days - 1).atStartOfDay();
    }

    /** Chart bucket size: days for 7 and 30 days, weeks for 90 days, months for all time. */
    public ChronoUnit bucketUnit() {
        return switch (this) {
            case D7, D30 -> ChronoUnit.DAYS;
            case D90 -> ChronoUnit.WEEKS;
            case ALL -> ChronoUnit.MONTHS;
        };
    }
}
