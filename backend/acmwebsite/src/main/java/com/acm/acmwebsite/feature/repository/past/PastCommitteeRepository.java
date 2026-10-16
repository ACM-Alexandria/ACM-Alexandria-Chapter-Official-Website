package com.acm.acmwebsite.feature.repository.past;

import com.acm.acmwebsite.feature.entity.past.PastCommittee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastCommitteeRepository extends JpaRepository<PastCommittee, Long> {
    boolean existsBySeason(Integer season);

    @Query("SELECT DISTINCT p.season FROM PastCommittee p")
    List<Integer> findDistinctSeasons();

    List<PastCommittee> findBySeasonOrderByNameAscIdAsc(Integer season);
}
