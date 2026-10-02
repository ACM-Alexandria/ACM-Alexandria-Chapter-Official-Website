package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    // Sets announcementSentAt only if it is still empty; returns 0 when someone already announced it
    @Modifying
    @Query("UPDATE Club x SET x.announcementSentAt = :sentAt WHERE x.id = :id AND x.announcementSentAt IS NULL")
    int claimAnnouncement(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);
}
