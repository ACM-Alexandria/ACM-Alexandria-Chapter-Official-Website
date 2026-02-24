package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
}
