package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ExclusiveRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExclusiveRegistrationRepository extends JpaRepository<ExclusiveRegistration, Long> {
    List<ExclusiveRegistration> findByExclusiveFormIdOrderByRegisteredAtAsc(Long formId);
    Optional<ExclusiveRegistration> findByUserIdAndExclusiveFormId(UUID userId, Long formId);
}
