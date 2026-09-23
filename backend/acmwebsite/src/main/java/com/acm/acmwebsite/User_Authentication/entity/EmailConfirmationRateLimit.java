package com.acm.acmwebsite.User_Authentication.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_confirmation_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfirmationRateLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "email", length = 255)
    private String email;

    @Column(nullable = false, name = "requested_at")
    private LocalDateTime requestedAt;
}
