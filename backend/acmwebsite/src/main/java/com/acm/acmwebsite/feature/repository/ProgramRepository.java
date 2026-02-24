package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program,Long> {
    List<Program> findByName(String name);
}
