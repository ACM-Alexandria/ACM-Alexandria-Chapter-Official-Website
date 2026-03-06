package com.acm.acmwebsite.User_Authentication.repository;

import com.acm.acmwebsite.User_Authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  void deleteByEmail(String email);
  Optional<User> findByResetPasswordToken(String resetPasswordToken);
}
