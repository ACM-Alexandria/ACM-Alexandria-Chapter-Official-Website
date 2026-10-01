package com.acm.acmwebsite.User_Authentication.repository;

import com.acm.acmwebsite.User_Authentication.entity.EmailConfirmationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EmailConfirmationRequestRepository extends JpaRepository<EmailConfirmationRequest, Long> {
    long countByEmailAndRequestedAtAfter(String email, LocalDateTime since);
}
