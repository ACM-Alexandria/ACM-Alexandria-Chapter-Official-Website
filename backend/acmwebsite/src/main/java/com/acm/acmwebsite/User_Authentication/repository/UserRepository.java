package com.acm.acmwebsite.User_Authentication.repository;

import com.acm.acmwebsite.User_Authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  void deleteByEmail(String email);
  Optional<User> findByResetPasswordToken(String resetPasswordToken);

  @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.department IS NOT NULL GROUP BY u.department")
  List<Object[]> countUsersByDepartment();

  @Query("SELECT u.batch, COUNT(u) FROM User u WHERE u.batch IS NOT NULL GROUP BY u.batch")
  List<Object[]> countUsersByBatch();

  @Query("SELECT u.createdAt FROM User u ORDER BY u.createdAt ASC")
  List<java.time.LocalDateTime> findAllCreatedTimes();
}
