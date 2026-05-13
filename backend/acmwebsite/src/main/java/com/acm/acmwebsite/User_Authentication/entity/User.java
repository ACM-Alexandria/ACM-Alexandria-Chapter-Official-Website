package com.acm.acmwebsite.User_Authentication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @NotBlank(message = "Password is required")
  @Column(nullable = false, name = "password_hash")
  private String passwordHash;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "reset_password_token", length = 255)
  private String resetPasswordToken;


  @Column(name = "reset_password_token_created_at")
  private LocalDateTime resetPasswordTokenCreatedAt;

  @Column(name = "forgot_password_count" , nullable = false)
  private Integer forgotPasswordCount = 0;

  @Column(name = "name")
  private String name;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "is_alex_eng_student")
  private Boolean isAlexEngStudent;

  @Enumerated(EnumType.STRING)
  @Column(name = "department")
  private Department department;

  @Column(name = "batch")
  private String batch;

  public enum Department {
      CSED,
      CCE
  }
}
