package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.RadioSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RadioSeasonRepository extends JpaRepository<RadioSeason, Long> {
    Optional<RadioSeason> findBySeasonNumber(Integer seasonNumber);
}
