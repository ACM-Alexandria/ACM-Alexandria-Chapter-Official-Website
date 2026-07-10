package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByName(String name);
    Page<Program> findAll(Pageable pageable);
}
