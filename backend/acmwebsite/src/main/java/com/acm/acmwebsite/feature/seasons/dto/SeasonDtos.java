package com.acm.acmwebsite.feature.seasons.dto;

import java.time.LocalDate;
import java.time.YearMonth;

import com.acm.acmwebsite.feature.seasons.PeriodType;
import com.acm.acmwebsite.feature.seasons.SeasonPeriod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SeasonDtos {

    private SeasonDtos() {
    }

    // A season (`name` ignored) or a quarter inside season `season`. No `endDate` means it is ongoing.
    public record PeriodRequest(
            @NotNull PeriodType type,
            @Size(max = 60) String name,
            @NotNull LocalDate startDate,
            LocalDate endDate,
            @NotNull @Min(2000) Integer season) {
    }

    public record MonthRequest(@NotNull YearMonth month) {
    }

    public record PeriodResponse(
            Long id,
            PeriodType type,
            String name,
            String label,
            LocalDate startDate,
            LocalDate endDate,
            Integer season) {

        public static PeriodResponse of(SeasonPeriod p) {
            return new PeriodResponse(p.getId(), p.getType(), p.getName(), p.label(), p.getStartDate(), p.getEndDate(), p.getSeason());
        }
    }

    // The current season (the latest one) with its dates.
    public record CurrentSeasonResponse(
            Integer season,
            String label,
            PeriodResponse period) {
    }
}
