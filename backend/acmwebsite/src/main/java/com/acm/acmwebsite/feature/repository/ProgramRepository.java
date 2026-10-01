package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByName(String name);
    Page<Program> findAll(Pageable pageable);

    // Sets announcementSentAt only if it is still empty; returns 0 when someone already announced it
    @Modifying
    @Query("UPDATE Program x SET x.announcementSentAt = :sentAt WHERE x.id = :id AND x.announcementSentAt IS NULL")
    int claimAnnouncement(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);
}
