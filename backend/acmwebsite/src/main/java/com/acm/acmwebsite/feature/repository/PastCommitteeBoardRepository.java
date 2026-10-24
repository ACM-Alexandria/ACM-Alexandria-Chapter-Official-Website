package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.PastCommitteeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastCommitteeBoardRepository extends JpaRepository<PastCommitteeBoard, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT DISTINCT p.season FROM PastCommitteeBoard p")
    List<Integer> findDistinctSeasons();

    // Keep null orders last and use the ID to make ties deterministic.
    @Query("SELECT p FROM PastCommitteeBoard p JOIN FETCH p.pastCommittee WHERE p.season = :season "
            + "ORDER BY CASE WHEN p.displayOrder IS NULL THEN 1 ELSE 0 END, p.displayOrder, p.id")
    List<PastCommitteeBoard> findBySeasonOrdered(Integer season);

    void deleteBySeason(Integer season);

    @Modifying
    @Query("UPDATE PastCommitteeBoard p SET p.season = :newSeason WHERE p.season = :season")
    int moveToSeason(@Param("season") Integer season, @Param("newSeason") Integer newSeason);
}
