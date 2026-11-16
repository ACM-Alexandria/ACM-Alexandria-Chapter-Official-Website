package com.acm.acmwebsite.feature.seasons;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeasonPeriodRepository extends JpaRepository<SeasonPeriod, Long> {

    Optional<SeasonPeriod> findByTypeAndStartDate(PeriodType type, LocalDate startDate);

    Optional<SeasonPeriod> findByTypeAndSeason(PeriodType type, Integer season);

    List<SeasonPeriod> findByTypeAndSeasonOrderByStartDateAsc(PeriodType type, Integer season);

    List<SeasonPeriod> findByTypeInOrderByStartDateDesc(List<PeriodType> types);

    // The latest season is the current one.
    Optional<SeasonPeriod> findTopByTypeOrderBySeasonDesc(PeriodType type);

    // The current season, locked for the archive transaction.
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM SeasonPeriod p WHERE p.type = :type ORDER BY p.season DESC")
    List<SeasonPeriod> findLatestForUpdate(@Param("type") PeriodType type, Pageable pageable);

    // Ongoing periods (no end date yet).
    List<SeasonPeriod> findByTypeAndEndDateIsNull(PeriodType type);

    @Query("SELECT p FROM SeasonPeriod p WHERE p.type = :type AND p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)")
    Optional<SeasonPeriod> findContaining(@Param("type") PeriodType type, @Param("date") LocalDate date);

    // Periods of `type` that share at least one day with [start, end]; an ongoing period runs on with no end.
    @Query("SELECT p FROM SeasonPeriod p WHERE p.type = :type AND p.startDate <= :end AND (p.endDate IS NULL OR p.endDate >= :start)")
    List<SeasonPeriod> findOverlapping(@Param("type") PeriodType type,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);
}
