package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.PastCommittee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastCommitteeRepository extends JpaRepository<PastCommittee, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT DISTINCT p.season FROM PastCommittee p")
    List<Integer> findDistinctSeasons();

    void deleteBySeason(Integer season);

    List<PastCommittee> findBySeasonOrderByNameAscIdAsc(Integer season);

    @Modifying
    @Query("UPDATE PastCommittee p SET p.season = :newSeason WHERE p.season = :season")
    int moveToSeason(@Param("season") Integer season, @Param("newSeason") Integer newSeason);
}
