package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.CommitteeRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommitteeRegistrationRepository extends JpaRepository<CommitteeRegistration, Long> {
    List<CommitteeRegistration> findByCommitteeCallId(Long committeeCallId);
    boolean existsByUserIdAndCommitteeCallId(UUID userId, Long committeeCallId);
    long countByCommitteeCallId(Long committeeCallId);
}
