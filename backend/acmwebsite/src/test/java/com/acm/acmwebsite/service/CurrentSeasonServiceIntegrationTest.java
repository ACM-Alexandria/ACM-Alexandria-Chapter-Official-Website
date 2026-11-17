package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.seasons.CurrentSeasonService;
import com.acm.acmwebsite.feature.seasons.PeriodType;
import com.acm.acmwebsite.feature.seasons.SeasonConflictException;
import com.acm.acmwebsite.feature.seasons.SeasonPeriod;
import com.acm.acmwebsite.feature.seasons.SeasonPeriodRepository;
import com.acm.acmwebsite.feature.seasons.SeasonPeriodService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

// The current season is the latest season in season_periods; there is no separate setting.
// The service is built directly (no Spring proxy); the archive-transaction rule is covered in
// PastSeasonsArchiveIntegrationTest.
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:current-season-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SeasonPeriodService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CurrentSeasonServiceIntegrationTest {

    @Autowired private SeasonPeriodRepository periods;
    @Autowired private SeasonPeriodService periodService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    private TransactionTemplate transaction;

    @BeforeEach
    void reset() {
        transaction = new TransactionTemplate(transactionManager);
        periods.deleteAllInBatch();
        jdbc.update("DELETE FROM system_settings");
    }

    @AfterEach
    void dropLegacyColumn() {
        jdbc.execute("ALTER TABLE system_settings DROP COLUMN IF EXISTS current_season");
    }

    private CurrentSeasonService at(String instant) {
        return new CurrentSeasonService(periods, periodService, jdbc, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private SeasonPeriod save(PeriodType type, String name, String start, String end, int season) {
        return periods.saveAndFlush(new SeasonPeriod(type, name, LocalDate.parse(start), end == null ? null : LocalDate.parse(end), season));
    }

    private SeasonPeriod season(int year) {
        return periods.findByTypeAndSeason(PeriodType.SEASON, year).orElseThrow();
    }

    @Test
    void firstSeasonFollowsTheDateWhenNothingWasStored() {
        assertEquals(2025, at("2026-08-15T12:00:00Z").getCurrentSeason());
        assertEquals(LocalDate.parse("2025-09-01"), season(2025).getStartDate());
        assertNull(season(2025).getEndDate());

        periods.deleteAllInBatch();
        assertEquals(2026, at("2026-09-01T12:00:00Z").getCurrentSeason());
        assertEquals(1, periods.count());
    }

    @Test
    void firstSeasonKeepsTheSeasonTheSiteUsedBefore() {
        jdbc.execute("ALTER TABLE system_settings ADD COLUMN current_season INT");
        jdbc.update("INSERT INTO system_settings (id, emails_enabled, current_season) VALUES (1, true, 2027)");

        assertEquals(2027, at("2026-10-01T12:00:00Z").getCurrentSeason());
        // Its 1 September is still ahead, so it starts today.
        assertEquals(LocalDate.parse("2026-10-01"), season(2027).getStartDate());
    }

    @Test
    void theLatestSeasonIsCurrent() {
        save(PeriodType.SEASON, null, "2025-09-01", "2026-08-31", 2025);
        save(PeriodType.SEASON, null, "2026-09-01", null, 2026);
        assertEquals(2026, at("2026-10-01T12:00:00Z").getCurrentSeason());
        assertEquals(2, periods.count());
    }

    @Test
    void startingTheNextSeasonEndsTheCurrentOneAndItsQuarterYesterday() {
        save(PeriodType.SEASON, null, "2026-09-01", null, 2026);
        save(PeriodType.QUARTER, "Q1", "2026-09-01", null, 2026);
        var service = at("2027-07-01T12:00:00Z");

        Integer next = transaction.execute(status -> service.startNextSeason(service.lockForArchive()));

        assertEquals(2027, next);
        assertEquals(LocalDate.parse("2027-06-30"), season(2026).getEndDate());
        assertEquals(LocalDate.parse("2027-06-30"),
                periods.findByTypeAndSeasonOrderByStartDateAsc(PeriodType.QUARTER, 2026).getFirst().getEndDate());
        assertEquals(LocalDate.parse("2027-07-01"), season(2027).getStartDate());
        assertNull(season(2027).getEndDate());
        assertEquals(2027, service.getCurrentSeason());
    }

    @Test
    void aSeasonThatAlreadyEndedKeepsItsEndDate() {
        save(PeriodType.SEASON, null, "2025-09-01", "2026-08-31", 2025);
        var service = at("2026-10-01T12:00:00Z");

        transaction.execute(status -> service.startNextSeason(service.lockForArchive()));

        assertEquals(LocalDate.parse("2026-08-31"), season(2025).getEndDate());
        assertEquals(LocalDate.parse("2026-10-01"), season(2026).getStartDate());
    }

    @Test
    void cannotFinishASeasonThatStartsTodayOrHasAFutureQuarter() {
        save(PeriodType.SEASON, null, "2026-10-01", null, 2026);
        var today = at("2026-10-01T12:00:00Z");
        assertThrows(BoardArchiveConflictException.class,
                () -> transaction.execute(status -> today.startNextSeason(today.lockForArchive())));

        periods.deleteAllInBatch();
        save(PeriodType.SEASON, null, "2026-09-01", null, 2026);
        save(PeriodType.QUARTER, "Q2", "2026-12-01", null, 2026);
        var october = at("2026-10-15T12:00:00Z");
        assertThrows(SeasonConflictException.class,
                () -> transaction.execute(status -> october.startNextSeason(october.lockForArchive())));
        // Nothing changed.
        assertNull(season(2026).getEndDate());
        assertTrue(periods.findByTypeAndSeason(PeriodType.SEASON, 2027).isEmpty());
    }

    @Test
    void aRolledBackFirstSeasonIsNotKept() {
        var service = at("2026-10-01T12:00:00Z");
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            assertEquals(2026, service.getCurrentSeason());
            throw new IllegalStateException("Simulated failure");
        }));
        assertEquals(0, periods.count());
    }
}
