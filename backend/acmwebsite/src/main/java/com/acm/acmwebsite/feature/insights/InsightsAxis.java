package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Bucket;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The time axis of a chart: bucket starts from the start of the period up to today, so periods
 * with no activity still show as zero. For "all", the earliest sample sets the start.
 */
public final class InsightsAxis {

    private final List<LocalDate> starts;
    private final ChronoUnit unit;

    private InsightsAxis(List<LocalDate> starts, ChronoUnit unit) {
        this.starts = starts;
        this.unit = unit;
    }

    public static InsightsAxis of(InsightsRange range, LocalDateTime now, Collection<LocalDateTime> samples) {
        ChronoUnit unit = range.bucketUnit();
        LocalDate today = now.toLocalDate();
        LocalDate first = range == InsightsRange.ALL ? earliest(samples, today) : range.since(now).toLocalDate();

        List<LocalDate> starts = new ArrayList<>();
        for (LocalDate day = align(first, unit); !day.isAfter(today); day = day.plus(1, unit)) {
            starts.add(day);
        }
        return new InsightsAxis(starts, unit);
    }

    public List<LocalDate> starts() {
        return starts;
    }

    /** Number of samples in each bucket. */
    public List<Bucket> count(Collection<LocalDateTime> samples) {
        Map<LocalDate, Long> totals = new HashMap<>();
        for (LocalDateTime sample : samples) {
            if (sample != null) totals.merge(align(sample.toLocalDate(), unit), 1L, Long::sum);
        }
        return toBuckets(totals);
    }

    /** Sum of values per bucket. Each row is {LocalDateTime time, Number value}. */
    public List<Bucket> sum(Collection<Object[]> rows) {
        Map<LocalDate, Long> totals = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] instanceof LocalDateTime time && row[1] instanceof Number value) {
                totals.merge(align(time.toLocalDate(), unit), value.longValue(), Long::sum);
            }
        }
        return toBuckets(totals);
    }

    private List<Bucket> toBuckets(Map<LocalDate, Long> totals) {
        List<Bucket> buckets = new ArrayList<>(starts.size());
        for (LocalDate start : starts) {
            buckets.add(new Bucket(start, totals.getOrDefault(start, 0L)));
        }
        return buckets;
    }

    private static LocalDate earliest(Collection<LocalDateTime> samples, LocalDate fallback) {
        LocalDate min = null;
        for (LocalDateTime sample : samples) {
            if (sample != null && (min == null || sample.toLocalDate().isBefore(min))) min = sample.toLocalDate();
        }
        return min == null ? fallback : min;
    }

    static LocalDate align(LocalDate day, ChronoUnit unit) {
        return switch (unit) {
            case WEEKS -> day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHS -> day.withDayOfMonth(1);
            default -> day;
        };
    }
}
