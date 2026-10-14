package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.SystemSettings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    // Only archive needs to hold this lock for the duration of its transaction.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SystemSettings s WHERE s.id = 1")
    Optional<SystemSettings> findSingletonForUpdate();
}
