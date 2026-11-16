package com.acm.acmwebsite.feature.badges;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.PeriodRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.PeriodResponse;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

// Seasons and quarters (admin-defined date ranges) and calendar months (created on first use).
@Service
@RequiredArgsConstructor
public class BadgePeriodService {

    private final BadgePeriodRepository periodRepository;
    private final BadgeAwardRepository awardRepository;

    // Seasons and quarters, newest first. Months are not listed; they are resolved by `resolveMonth`.
    @Transactional(readOnly = true)
    public List<PeriodResponse> listSeasonsAndQuarters() {
        return periodRepository.findByTypeInOrderByStartDateDesc(List.of(PeriodType.SEASON, PeriodType.QUARTER))
                .stream().map(BadgeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgePeriod get(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge period not found"));
    }

    @Transactional
    public PeriodResponse create(PeriodRequest request) {
        BadgePeriod period = new BadgePeriod();
        period.setType(request.type());
        apply(period, request);
        return BadgeMapper.toResponse(periodRepository.save(period));
    }

    @Transactional
    public PeriodResponse update(Long id, PeriodRequest request) {
        BadgePeriod period = get(id);
        if (period.getType() != request.type()) {
            throw new IllegalArgumentException("A period's type can't be changed");
        }
        apply(period, request);
        return BadgeMapper.toResponse(period);
    }

    @Transactional
    public void delete(Long id) {
        BadgePeriod period = get(id);
        if (awardRepository.existsByPeriod_Id(id)) {
            throw new BadgeConflictException(period.label() + " has badges awarded and can't be deleted");
        }
        if (period.getType() == PeriodType.SEASON
                && !periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, period.getSeason()).isEmpty()) {
            throw new BadgeConflictException("Delete the quarters of " + period.label() + " first");
        }
        periodRepository.delete(period);
    }

    // The period for a calendar month, created the first time it is asked for.
    @Transactional
    public BadgePeriod resolveMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        return periodRepository.findByTypeAndStartDate(PeriodType.MONTH, start).orElseGet(() -> {
            Integer season = periodRepository.findContaining(PeriodType.SEASON, start)
                    .map(BadgePeriod::getSeason).orElse(null);
            return periodRepository.save(new BadgePeriod(PeriodType.MONTH, null, start, month.atEndOfMonth(), season));
        });
    }

    // The period of `type` that includes `date`: always a month, or the admin-defined quarter or season if one covers it.
    @Transactional
    public Optional<BadgePeriod> current(PeriodType type, LocalDate date) {
        if (type == PeriodType.MONTH) {
            return Optional.of(resolveMonth(YearMonth.from(date)));
        }
        return periodRepository.findContaining(type, date);
    }

    private void apply(BadgePeriod period, PeriodRequest request) {
        if (request.type() == PeriodType.MONTH) {
            throw new IllegalArgumentException("Months are created automatically");
        }
        LocalDate start = request.startDate();
        LocalDate end = request.endDate();
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("The end date must be on or after the start date");
        }

        if (request.type() == PeriodType.QUARTER) {
            String name = request.name() == null ? "" : request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("A quarter needs a name, such as Q1");
            }
            BadgePeriod season = periodRepository.findByTypeAndSeason(PeriodType.SEASON, request.season())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Create season " + BadgePeriod.seasonLabel(request.season()) + " before adding quarters to it"));
            if (start.isBefore(season.getStartDate()) || end.isAfter(season.getEndDate())) {
                throw new IllegalArgumentException("The quarter must fall within " + season.label()
                        + " (" + season.getStartDate() + " to " + season.getEndDate() + ")");
            }
            period.setName(name);
        } else {
            periodRepository.findByTypeAndSeason(PeriodType.SEASON, request.season())
                    .filter(other -> !Objects.equals(other.getId(), period.getId()))
                    .ifPresent(other -> {
                        throw new BadgeConflictException(other.label() + " already exists");
                    });
            if (period.getId() != null) {
                checkQuartersStillFit(period, request.season(), start, end);
            }
            period.setName(null);
        }

        periodRepository.findOverlapping(request.type(), start, end).stream()
                .filter(other -> !Objects.equals(other.getId(), period.getId()))
                .findFirst()
                .ifPresent(other -> {
                    throw new BadgeConflictException("These dates overlap " + other.label()
                            + " (" + other.getStartDate() + " to " + other.getEndDate() + ")");
                });

        period.setStartDate(start);
        period.setEndDate(end);
        period.setSeason(request.season());
    }

    // Editing a season can't strand its quarters outside it or under another season number.
    private void checkQuartersStillFit(BadgePeriod season, Integer newSeason, LocalDate start, LocalDate end) {
        List<BadgePeriod> quarters = periodRepository.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, season.getSeason());
        if (quarters.isEmpty()) {
            return;
        }
        if (!Objects.equals(newSeason, season.getSeason())) {
            throw new BadgeConflictException("This season has quarters, so its year can't change");
        }
        for (BadgePeriod q : quarters) {
            if (q.getStartDate().isBefore(start) || q.getEndDate().isAfter(end)) {
                throw new BadgeConflictException(q.label() + " (" + q.getStartDate() + " to " + q.getEndDate()
                        + ") would fall outside the new season dates");
            }
        }
    }
}
