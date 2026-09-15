package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.HighBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HighBoardRepository extends JpaRepository<HighBoard, Long> {
    Optional<HighBoard> findByUserId(UUID userId);

    @Query("SELECT h FROM HighBoard h JOIN FETCH h.user")
    List<HighBoard> findAllWithUser();
}

