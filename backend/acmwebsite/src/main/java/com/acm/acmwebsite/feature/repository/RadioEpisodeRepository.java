package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.RadioEpisode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RadioEpisodeRepository extends JpaRepository<RadioEpisode, Long> {
    List<RadioEpisode> findByRadioSeasonIdOrderByEpisodeNumberAsc(Long radioSeasonId);
    Page<RadioEpisode> findByRadioSeasonId(Long radioSeasonId, Pageable pageable);
}
