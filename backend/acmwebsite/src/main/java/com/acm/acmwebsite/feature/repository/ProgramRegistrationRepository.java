package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ProgramRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgramRegistrationRepository extends JpaRepository<ProgramRegistration, Long> {
    boolean existsByUserIdAndProgramId(UUID userId, Long programId);

    List<ProgramRegistration> findByProgramId(Long programId);

    @Query("SELECT r.program.id, r.program.name, COUNT(r) FROM ProgramRegistration r GROUP BY r.program.id, r.program.name")
    List<Object[]> countRegistrationsByProgram();
}
