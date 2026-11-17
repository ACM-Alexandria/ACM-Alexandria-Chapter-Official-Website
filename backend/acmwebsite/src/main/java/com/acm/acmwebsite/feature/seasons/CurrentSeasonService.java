package com.acm.acmwebsite.feature.seasons;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.CurrentSeasonResponse;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodRequest;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;

// The chapter's current season: the latest season in Season settings (season_periods). Boards, past boards
// and archiving all read it from here; there is no separate "current season" setting.
@Service
public class CurrentSeasonService {

    private final SeasonPeriodRepository periodRepository;
    private final SeasonPeriodService periodService;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    @Autowired
    public CurrentSeasonService(SeasonPeriodRepository periodRepository, SeasonPeriodService periodService, JdbcTemplate jdbc) {
        this(periodRepository, periodService, jdbc, Clock.systemDefaultZone());
    }

    // Tests supply a fixed clock.
    public CurrentSeasonService(SeasonPeriodRepository periodRepository, SeasonPeriodService periodService,
                                JdbcTemplate jdbc, Clock clock) {
        this.periodRepository = periodRepository;
        this.periodService = periodService;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // The current season's year. If no season exists yet, the first one is created (see `createFirstSeason`).
    @Transactional
    public Integer getCurrentSeason() {
        return periodRepository.findTopByTypeOrderBySeasonDesc(PeriodType.SEASON)
                .orElseGet(this::createFirstSeason)
                .getSeason();
    }

    @Transactional
    public CurrentSeasonResponse currentSeasonResponse() {
        SeasonPeriod current = periodRepository.findTopByTypeOrderBySeasonDesc(PeriodType.SEASON)
                .orElseGet(this::createFirstSeason);
        return new CurrentSeasonResponse(current.getSeason(), current.label(), PeriodResponse.of(current));
    }

    // Locks the current season row for the archive transaction, so two archives can't run at once.
    @Transactional(propagation = Propagation.MANDATORY)
    public SeasonPeriod lockForArchive() {
        getCurrentSeason();
        return periodRepository.findLatestForUpdate(PeriodType.SEASON, PageRequest.of(0, 1)).getFirst();
    }

    // Archiving finishes `current`: it ends yesterday (with its ongoing quarter) and the next season starts
    // today with no end date. Returns the new season's year.
    @Transactional(propagation = Propagation.MANDATORY)
    public Integer startNextSeason(SeasonPeriod current) {
        LocalDate today = LocalDate.now(clock);
        if (!current.getStartDate().isBefore(today)) {
            throw new BoardArchiveConflictException(current.label() + " starts on " + current.getStartDate()
                    + ", so it can't end before today. Change its start date in Season settings first. Nothing was archived.");
        }
        periodService.endSeason(current, today.minusDays(1));
        int next = current.getSeason() + 1;
        periodService.create(new PeriodRequest(PeriodType.SEASON, null, today, null, next));
        return next;
    }

    // First run: start from the season number the site used before Season settings existed (the old
    // system_settings.current_season column), else from the date (seasons start in September).
    // The season starts on 1 September of its year, or today if that is still ahead, and is ongoing.
    private SeasonPeriod createFirstSeason() {
        LocalDate today = LocalDate.now(clock);
        Integer season = readLegacyCurrentSeason();
        if (season == null) {
            season = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        }
        LocalDate start = LocalDate.of(season, 9, 1);
        if (start.isAfter(today)) {
            start = today;
        }
        return periodRepository.saveAndFlush(new SeasonPeriod(PeriodType.SEASON, null, start, null, season));
    }

    private Integer readLegacyCurrentSeason() {
        try {
            return jdbc.queryForObject("SELECT current_season FROM system_settings WHERE id = 1", Integer.class);
        } catch (DataAccessException e) {
            // No settings row, or a database created after the column was removed.
            return null;
        }
    }
}
