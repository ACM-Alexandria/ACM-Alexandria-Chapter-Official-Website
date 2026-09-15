package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ClubBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubBoardRepository extends JpaRepository<ClubBoard, Long> {
    Optional<ClubBoard> findByUserId(UUID userId);
    java.util.List<ClubBoard> findByClubId(Long clubId);
}
