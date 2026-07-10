package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ClubRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClubRegistrationRepository extends JpaRepository<ClubRegistration, Long> {
    boolean existsByUserIdAndClubId(UUID userId, Long clubId);

    List<ClubRegistration> findByClubId(Long clubId);

    void deleteByClubId(Long clubId);

    @Query("SELECT r.club.id, r.club.name, COUNT(r) FROM ClubRegistration r GROUP BY r.club.id, r.club.name")
    List<Object[]> countRegistrationsByClub();
}
