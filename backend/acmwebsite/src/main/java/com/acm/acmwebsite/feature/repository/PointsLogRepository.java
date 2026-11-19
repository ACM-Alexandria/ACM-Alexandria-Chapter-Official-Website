package com.acm.acmwebsite.feature.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.feature.entity.PointsLog;

public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {

    Page<PointsLog> findByMember_Id(UUID id, Pageable pageable);

    Page<PointsLog> findByAssigner_Id(UUID id, Pageable pageable);

    /** One member's points total within a period, with the committee they are in now. */
    interface MemberPointsTotal {
        UUID getMemberId();

        /** Null when the member has no committee. */
        Long getCommitteeId();

        Long getTotal();
    }

    /**
     * Points each member with {@code role} earned in [start, end), counted by when each log was created.
     * Only positive totals are returned, highest first. Badge suggestions group the rows by committee;
     * the leaderboard reads them as one chapter-wide ranking.
     */
    @Query("""
            SELECT m.id AS memberId, c.id AS committeeId, SUM(l.points) AS total
            FROM PointsLog l
            JOIN l.member m
            LEFT JOIN m.committee c
            WHERE l.createdAt >= :start AND l.createdAt < :end
              AND m.role = :role
            GROUP BY m.id, c.id
            HAVING SUM(l.points) > 0
            ORDER BY SUM(l.points) DESC
            """)
    List<MemberPointsTotal> sumPointsByMemberBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end,
                                                     @Param("role") Role role);

    /** The same totals for people with any of {@code roles}. */
    @Query("""
            SELECT m.id AS memberId, c.id AS committeeId, SUM(l.points) AS total
            FROM PointsLog l
            JOIN l.member m
            LEFT JOIN m.committee c
            WHERE l.createdAt >= :start AND l.createdAt < :end
              AND m.role IN :roles
            GROUP BY m.id, c.id
            HAVING SUM(l.points) > 0
            ORDER BY SUM(l.points) DESC
            """)
    List<MemberPointsTotal> sumPointsByMemberBetweenForRoles(@Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end,
                                                             @Param("roles") java.util.Collection<Role> roles);
}
