package com.acm.acmwebsite.feature.seasons;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.ObjectProvider;

import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodRequest;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;

import lombok.RequiredArgsConstructor;

// Seasons and quarters (date ranges the super admin sets) and calendar months (created on first use).
// Generic: features such as badges look periods up here instead of keeping their own calendar.
@Service
@RequiredArgsConstructor
public class SeasonPeriodService {

    private final SeasonPeriodRepository periodRepository;
    // Every feature that refers to periods (badges, ...); may be none.
    private final ObjectProvider<PeriodUsage> usages;

    // Seasons and quarters, newest first. Months are not listed; they are resolved by `resolveMonth`.
    @Transactional(readOnly = true)
    public List<PeriodResponse> listSeasonsAndQuarters() {
        return periodRepository.findByTypeInOrderByStartDateDesc(List.of(PeriodType.SEASON, PeriodType.QUARTER))
                .stream().map(PeriodResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public SeasonPeriod get(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));
    }

    @Transactional
    public PeriodResponse create(PeriodRequest request) {
        SeasonPeriod period = new SeasonPeriod();
        period.setType(request.type());
        apply(period, request);
        return PeriodResponse.of(periodRepository.save(period));
    }

    @Transactional
    public PeriodResponse update(Long id, PeriodRequest request) {
        SeasonPeriod period = get(id);
        if (period.getType() != request.type()) {
            throw new IllegalArgumentException("A period's type can't be changed");
        }
        apply(period, request);
        return PeriodResponse.of(period);
    }

    // A period a feature refers to (see PeriodUsage) can't be deleted.
    @Transactional
    public void delete(Long id) {
        SeasonPeriod period = get(id);
        if (period.getType() == PeriodType.SEASON
                && !periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, period.getSeason()).isEmpty()) {
            throw new SeasonConflictException("Delete the quarters of " + period.label() + " first");
        }
        for (PeriodUsage usage : usages.orderedStream().toList()) {
            if (usage.isPeriodInUse(id)) {
                throw new SeasonConflictException(period.label() + " is in use (" + usage.usageDescription() + ") and can't be deleted");
            }
        }
        periodRepository.delete(period);
    }

    // The period for a calendar month, created the first time it is asked for.
    @Transactional
    public SeasonPeriod resolveMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        return periodRepository.findByTypeAndStartDate(PeriodType.MONTH, start).orElseGet(() -> {
            Integer season = periodRepository.findContaining(PeriodType.SEASON, start)
                    .map(SeasonPeriod::getSeason).orElse(null);
            return periodRepository.save(new SeasonPeriod(PeriodType.MONTH, null, start, month.atEndOfMonth(), season));
        });
    }

    // The period of `type` that includes `date`: always a month, or the quarter or season that covers it, if set.
    @Transactional
    public Optional<SeasonPeriod> current(PeriodType type, LocalDate date) {
        if (type == PeriodType.MONTH) {
            return Optional.of(resolveMonth(YearMonth.from(date)));
        }
        return periodRepository.findContaining(type, date);
    }

    // A null end date means the period is ongoing (the current season or quarter, whose end isn't known yet).
    private void apply(SeasonPeriod period, PeriodRequest request) {
        if (request.type() == PeriodType.MONTH) {
            throw new IllegalArgumentException("Months are created automatically");
        }
        LocalDate start = request.startDate();
        LocalDate end = request.endDate();
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("The end date must be on or after the start date");
        }

        if (request.type() == PeriodType.QUARTER) {
            String name = request.name() == null ? "" : request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("A quarter needs a name, such as Q1");
            }
            SeasonPeriod season = periodRepository.findByTypeAndSeason(PeriodType.SEASON, request.season())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Create season " + SeasonPeriod.seasonLabel(request.season()) + " before adding quarters to it"));
            if (start.isBefore(season.getStartDate())) {
                throw new IllegalArgumentException("The quarter can't start before " + season.label() + " (" + season.getStartDate() + ")");
            }
            if (season.getEndDate() != null) {
                if (end == null) {
                    throw new IllegalArgumentException(season.label() + " ended on " + season.getEndDate()
                            + ", so the quarter needs an end date");
                }
                if (end.isAfter(season.getEndDate())) {
                    throw new IllegalArgumentException("The quarter must end by " + season.getEndDate()
                            + ", when " + season.label() + " ends");
                }
            }
            period.setName(name);
        } else {
            periodRepository.findByTypeAndSeason(PeriodType.SEASON, request.season())
                    .filter(other -> !Objects.equals(other.getId(), period.getId()))
                    .ifPresent(other -> {
                        throw new SeasonConflictException(other.label() + " already exists");
                    });
            if (period.getId() != null) {
                if (!Objects.equals(request.season(), period.getSeason())
                        && !periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, period.getSeason()).isEmpty()) {
                    throw new SeasonConflictException("This season has quarters, so its year can't change");
                }
                fitQuarters(period.getSeason(), start, end);
            }
            period.setName(null);
        }

        // Starting a new period ends the ongoing one before it, the day before the new one starts.
        closeOngoingBefore(request.type(), start, period.getId());

        periodRepository.findOverlapping(request.type(), start, end == null ? OPEN_END : end).stream()
                .filter(other -> !Objects.equals(other.getId(), period.getId()))
                .findFirst()
                .ifPresent(other -> {
                    throw new SeasonConflictException("These dates overlap " + other.label() + " (" + describe(other) + ")");
                });

        period.setStartDate(start);
        period.setEndDate(end);
        period.setSeason(request.season());
    }

    // Ends a season on `end` (or keeps an earlier end it already has), ending its ongoing quarter with it.
    // Used when archiving finishes the season.
    @Transactional
    public void endSeason(SeasonPeriod season, LocalDate end) {
        LocalDate last = season.getEndDate() != null && season.getEndDate().isBefore(end) ? season.getEndDate() : end;
        for (SeasonPeriod q : periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, season.getSeason())) {
            if (q.getStartDate().isAfter(last)) {
                throw new SeasonConflictException(q.label() + " starts on " + q.getStartDate() + ", after " + season.label()
                        + " would end (" + last + "). Change or delete it in Season settings first.");
            }
            if (q.getEndDate() == null || q.getEndDate().isAfter(last)) {
                q.setEndDate(last);
            }
        }
        season.setEndDate(last);
    }

    // Stands in for "no end yet" in date comparisons.
    private static final LocalDate OPEN_END = LocalDate.of(9999, 12, 31);

    private void closeOngoingBefore(PeriodType type, LocalDate start, Long exceptId) {
        for (SeasonPeriod open : periodRepository.findByTypeAndEndDateIsNull(type)) {
            if (Objects.equals(open.getId(), exceptId) || !open.getStartDate().isBefore(start)) {
                continue;
            }
            LocalDate dayBefore = start.minusDays(1);
            if (type == PeriodType.SEASON) {
                fitQuarters(open.getSeason(), open.getStartDate(), dayBefore);
            }
            open.setEndDate(dayBefore);
        }
    }

    // Keeps a season's quarters inside its dates. When the season gets an end date, an ongoing quarter
    // ends with it; a quarter that would fall outside the season is refused.
    private void fitQuarters(Integer season, LocalDate start, LocalDate end) {
        for (SeasonPeriod q : periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, season)) {
            boolean startsTooEarly = q.getStartDate().isBefore(start);
            boolean endsTooLate = end != null && (q.getStartDate().isAfter(end)
                    || (q.getEndDate() != null && q.getEndDate().isAfter(end)));
            if (startsTooEarly || endsTooLate) {
                throw new SeasonConflictException(q.label() + " (" + describe(q) + ") would fall outside the season's dates");
            }
            if (end != null && q.getEndDate() == null) {
                q.setEndDate(end);
            }
        }
    }

    private static String describe(SeasonPeriod p) {
        return p.getEndDate() == null ? "from " + p.getStartDate() + ", ongoing" : p.getStartDate() + " to " + p.getEndDate();
    }
}
