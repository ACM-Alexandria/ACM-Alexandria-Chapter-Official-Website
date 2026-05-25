package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitteeBoardRepository extends JpaRepository<CommitteeBoard, Long> {
}
