package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.CommitteeCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDateTime;

@Repository
public interface CommitteeCallRepository extends JpaRepository<CommitteeCall, Long> {

    List<CommitteeCall> findByCommitteeIdOrderByOpenedAtDesc(Long committeeId);

    @Query("SELECT c FROM CommitteeCall c WHERE c.committee.id = :committeeId AND c.closedAt IS NULL")
    Optional<CommitteeCall> findActiveCallByCommitteeId(@Param("committeeId") Long committeeId);

    // Sets announcementSentAt only if it is still empty; returns 0 when someone already announced it
    @Modifying
    @Query("UPDATE CommitteeCall x SET x.announcementSentAt = :sentAt WHERE x.id = :id AND x.announcementSentAt IS NULL")
    int claimAnnouncement(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);
}
