package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Bucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsightsAxisTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 14, 30);

    @Test
    @DisplayName("period parsing falls back to 30 days")
    void parsesPeriods() {
        assertEquals(InsightsRange.D7, InsightsRange.parse("7d"));
        assertEquals(InsightsRange.D90, InsightsRange.parse("90D"));
        assertEquals(InsightsRange.ALL, InsightsRange.parse("all"));
        assertEquals(InsightsRange.D30, InsightsRange.parse("other"));
        assertEquals(InsightsRange.D30, InsightsRange.parse(null));
    }

    @Test
    @DisplayName("7 days starts at midnight six days back, today included")
    void sevenDaysStartsAtMidnight() {
        assertEquals(LocalDateTime.of(2026, 10, 17, 0, 0), InsightsRange.D7.since(NOW));
    }

    @Test
    @DisplayName("7-day axis has 7 daily buckets, empty days as zero")
    void sevenDayAxis() {
        InsightsAxis axis = InsightsAxis.of(InsightsRange.D7, NOW, List.of());
        assertEquals(7, axis.starts().size());
        List<Bucket> buckets = axis.count(List.of(NOW, NOW.minusDays(2), NOW.minusDays(2)));
        assertEquals(1L, buckets.get(6).value());
        assertEquals(2L, buckets.get(4).value());
        assertEquals(0L, buckets.get(5).value());
    }

    @Test
    @DisplayName("90-day axis uses weeks starting on Monday")
    void ninetyDayAxisUsesWeeks() {
        List<LocalDate> starts = InsightsAxis.of(InsightsRange.D90, NOW, List.of()).starts();
        assertEquals(DayOfWeek.MONDAY, starts.get(0).getDayOfWeek());
        for (int i = 1; i < starts.size(); i++) {
            assertEquals(7, ChronoUnit.DAYS.between(starts.get(i - 1), starts.get(i)));
        }
    }

    @Test
    @DisplayName("all-time axis starts at the earliest sample's month")
    void allTimeAxisStartsAtEarliestMonth() {
        List<LocalDate> starts = InsightsAxis.of(InsightsRange.ALL, NOW, List.of(NOW, LocalDateTime.of(2026, 3, 14, 9, 0))).starts();
        assertEquals(LocalDate.of(2026, 3, 1), starts.get(0));
        assertEquals(LocalDate.of(2026, 10, 1), starts.get(starts.size() - 1));
    }

    @Test
    @DisplayName("sum adds values per bucket")
    void sumsValues() {
        InsightsAxis axis = InsightsAxis.of(InsightsRange.D7, NOW, List.of());
        List<Bucket> buckets = axis.sum(List.of(new Object[]{NOW, 10}, new Object[]{NOW, 5}, new Object[]{NOW.minusDays(1), 3}));
        assertEquals(15L, buckets.get(6).value());
        assertEquals(3L, buckets.get(5).value());
    }
}
