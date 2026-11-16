package com.acm.acmwebsite.feature.badges;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BadgePeriodRepository extends JpaRepository<BadgePeriod, Long> {

    Optional<BadgePeriod> findByTypeAndStartDate(PeriodType type, LocalDate startDate);

    Optional<BadgePeriod> findByTypeAndSeason(PeriodType type, Integer season);

    List<BadgePeriod> findByTypeAndSeasonOrderByStartDateAsc(PeriodType type, Integer season);

    List<BadgePeriod> findByTypeInOrderByStartDateDesc(List<PeriodType> types);

    @Query("SELECT p FROM BadgePeriod p WHERE p.type = :type AND p.startDate <= :date AND p.endDate >= :date")
    Optional<BadgePeriod> findContaining(@Param("type") PeriodType type, @Param("date") LocalDate date);

    // Periods of `type` that share at least one day with [start, end].
    @Query("SELECT p FROM BadgePeriod p WHERE p.type = :type AND p.startDate <= :end AND p.endDate >= :start")
    List<BadgePeriod> findOverlapping(@Param("type") PeriodType type,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
