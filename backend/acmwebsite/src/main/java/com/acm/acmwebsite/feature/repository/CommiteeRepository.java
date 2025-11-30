package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommiteeRepository extends JpaRepository<Committee, Long> {

    boolean existsCommitteeByName(String name);
}
