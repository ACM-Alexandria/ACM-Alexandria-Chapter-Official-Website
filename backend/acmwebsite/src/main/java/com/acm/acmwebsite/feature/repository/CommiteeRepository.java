package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommiteeRepository extends JpaRepository<Committee, Long> {

    boolean existsCommitteeByName(String name);

    @Query("select c from Committee c left join fetch c.messageForCalls")
    List<Committee> getAll();
}
