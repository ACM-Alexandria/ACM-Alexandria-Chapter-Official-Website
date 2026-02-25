package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommitteeRepository extends JpaRepository<Committee, Long> {

    boolean existsCommitteeByName(String name);

    @Query("select distinct c from Committee c left join fetch c.messageForCalls left join fetch c.boardRoles")
    List<Committee> getAll();

    @Query("select distinct c from Committee c left join fetch c.messageForCalls left join fetch c.boardRoles where c.id = :id")
    Optional<Committee> findWithDetailsById(@Param("id") Long id);
}
