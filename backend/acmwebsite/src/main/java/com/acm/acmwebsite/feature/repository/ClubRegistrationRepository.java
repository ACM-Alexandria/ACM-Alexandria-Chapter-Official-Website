package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ClubRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClubRegistrationRepository extends JpaRepository<ClubRegistration, Long> {
    boolean existsByUserIdAndClubId(UUID userId, Long clubId);
}
