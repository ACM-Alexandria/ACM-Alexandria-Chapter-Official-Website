package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.PastSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastSeasonRepository extends JpaRepository<PastSeason, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT s.season FROM PastSeason s")
    List<Integer> findAllSeasons();

    void deleteBySeason(Integer season);
}
