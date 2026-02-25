package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.HighBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HighBoardRepository extends JpaRepository<HighBoard, Long> {
}
