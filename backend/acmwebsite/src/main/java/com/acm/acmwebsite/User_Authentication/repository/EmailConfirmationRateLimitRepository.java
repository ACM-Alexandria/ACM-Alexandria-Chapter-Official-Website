package com.acm.acmwebsite.User_Authentication.repository;

import com.acm.acmwebsite.User_Authentication.entity.EmailConfirmationRateLimit;
import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface EmailConfirmationRateLimitRepository extends JpaRepository<EmailConfirmationRateLimit, UUID> {
    long countByEmailAndRequestedAtAfter(String email, LocalDateTime since);
}
