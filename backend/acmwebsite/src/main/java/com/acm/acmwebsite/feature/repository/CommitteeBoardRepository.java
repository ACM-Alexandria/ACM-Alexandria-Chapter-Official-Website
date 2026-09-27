package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommitteeBoardRepository extends JpaRepository<CommitteeBoard, Long> {
    Optional<CommitteeBoard> findByUserId(UUID userId);

    @Query("SELECT cb FROM CommitteeBoard cb JOIN FETCH cb.user JOIN FETCH cb.committee")
    List<CommitteeBoard> findAllWithUserAndCommittee();
}
