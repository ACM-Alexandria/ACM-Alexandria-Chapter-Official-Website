package com.acm.acmwebsite.User_Authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

/** RefreshToken */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "refresh_tokens",
    uniqueConstraints = @UniqueConstraint(columnNames = "refresh_token"),
    indexes = {@Index(columnList = "user_id"), @Index(columnList = "refresh_token")})
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @JoinColumn(nullable = false, name = "user_id")
  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @NotBlank
  @Column(nullable = false, name = "refresh_token", unique = true)
  private String refreshToken;

  @NotNull
  @Column(nullable = false, name = "refresh_token_expiry")
  private LocalDateTime refreshTokenExpiry;

  @Builder.Default
  @Column(nullable = false, name = "soft_delete", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean softDelete = false;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private LocalDateTime updatedAt;
}
