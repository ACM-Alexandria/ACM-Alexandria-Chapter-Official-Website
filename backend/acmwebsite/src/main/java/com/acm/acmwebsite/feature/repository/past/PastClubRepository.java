package com.acm.acmwebsite.feature.repository.past;

import com.acm.acmwebsite.feature.entity.past.PastClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastClubRepository extends JpaRepository<PastClub, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT DISTINCT p.season FROM PastClub p")
    List<Integer> findDistinctSeasons();

    List<PastClub> findBySeasonOrderByNameAscIdAsc(Integer season);
}
