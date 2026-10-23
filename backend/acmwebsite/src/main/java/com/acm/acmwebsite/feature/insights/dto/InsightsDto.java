package com.acm.acmwebsite.feature.insights.dto;

import java.time.LocalDate;
import java.util.List;

/** Response shapes for the admin Insights tab. */
public final class InsightsDto {

    private InsightsDto() {
    }

    /**
     * A headline figure. value is all-time; inPeriod is the part that falls in the chosen period
     * (null when the figure has no time, like "open right now"). trend is the per-bucket series
     * behind inPeriod, for a small trend line; it is null when not shown.
     */
    public record Kpi(String key, String label, long value, Long inPeriod, List<Long> trend) {
    }

    public record Bucket(LocalDate start, long value) {
    }

    public record Series(String key, String label, List<Bucket> points) {
    }

    /** kind: "area" (one series over time) or "stacked" (several series stacked per bucket). */
    public record Chart(String key, String title, String kind, List<Series> series) {
    }

    /** One row of a ranking. detail is a short second line (a date, a committee), or null. */
    public record RankItem(String label, String detail, long value) {
    }

    public record Ranking(String key, String title, List<RankItem> items) {
    }

    /** Something open right now: a committee call, a registration form or an upcoming event. */
    public record OpenItem(String type, String name, String detail, long registrations) {
    }

    /** A state rather than a number, like the site email lock. tone: "success", "danger" or "neutral". */
    public record Status(String label, String value, String tone) {
    }

    /** The global overview shown above the section tabs. */
    public record Overview(String range, List<Kpi> kpis, List<Chart> charts, List<OpenItem> openNow) {
    }

    /** One tab under the overview: committees, events, clubs, programs, points, radio or communication. */
    public record Section(
            String id,
            String range,
            List<Kpi> kpis,
            List<Chart> charts,
            List<Ranking> rankings,
            List<Status> statuses) {
    }
}
