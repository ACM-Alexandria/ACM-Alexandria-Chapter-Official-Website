package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByUserIdAndEventId(UUID userId, Long eventId);
}
