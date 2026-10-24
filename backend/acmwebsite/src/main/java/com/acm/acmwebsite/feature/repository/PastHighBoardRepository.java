package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.PastHighBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastHighBoardRepository extends JpaRepository<PastHighBoard, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT DISTINCT p.season FROM PastHighBoard p")
    List<Integer> findDistinctSeasons();

    // Keep null orders last and use the ID to make ties deterministic.
    @Query("SELECT p FROM PastHighBoard p WHERE p.season = :season "
            + "ORDER BY CASE WHEN p.displayOrder IS NULL THEN 1 ELSE 0 END, p.displayOrder, p.id")
    List<PastHighBoard> findBySeasonOrdered(Integer season);

    void deleteBySeason(Integer season);

    @Modifying
    @Query("UPDATE PastHighBoard p SET p.season = :newSeason WHERE p.season = :season")
    int moveToSeason(@Param("season") Integer season, @Param("newSeason") Integer newSeason);
}
