package com.acm.acmwebsite.repository;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository.MemberPointsTotal;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
public class PointsLogRepositoryTest {

    @Autowired
    private PointsLogRepository pointsLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommitteeRepository committeeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByMember_Id_ShouldReturnPagedResults() {
        User member = new User();
        member.setEmail("member@test.com");
        member.setPasswordHash("hash");
        member = userRepository.save(member);

        User assigner = new User();
        assigner.setEmail("assigner@test.com");
        assigner.setPasswordHash("hash");
        assigner = userRepository.save(assigner);

        PointsLog log = new PointsLog();
        log.setMember(member);
        log.setAssigner(assigner);
        log.setPoints(10);
        log.setReason("Event");
        pointsLogRepository.save(log);

        Page<PointsLog> result = pointsLogRepository.findByMember_Id(member.getId(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Event", result.getContent().get(0).getReason());
    }

    @Test
    void sumPointsByMemberBetween_ShouldTotalMembersInRangeHighestFirst() {
        Committee technical = new Committee();
        technical.setName("Technical");
        technical.setDescription("Builds things");
        technical = committeeRepository.save(technical);

        User assigner = saveUser("board@test.com", Role.ACM_COMMITTEE_BOARD, technical);
        User nour = saveUser("nour@test.com", Role.ACM_MEMBER, technical);
        User salma = saveUser("salma@test.com", Role.ACM_MEMBER, null);
        User board = saveUser("other.board@test.com", Role.ACM_COMMITTEE_BOARD, technical);
        User negative = saveUser("negative@test.com", Role.ACM_MEMBER, technical);

        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 1, 0, 0);

        // Nour: 30 + 20 inside November; the October and December logs don't count.
        saveLog(nour, assigner, 30, start);
        saveLog(nour, assigner, 20, end.minusSeconds(1));
        saveLog(nour, assigner, 500, start.minusSeconds(1));
        saveLog(nour, assigner, 500, end);
        // Salma: 60 inside November, no committee.
        saveLog(salma, assigner, 60, start.plusDays(10));
        // Board members are not ranked.
        saveLog(board, assigner, 999, start.plusDays(3));
        // A member whose November total is not positive is left out.
        saveLog(negative, assigner, 10, start.plusDays(2));
        saveLog(negative, assigner, -15, start.plusDays(4));
        entityManager.clear();

        List<MemberPointsTotal> totals = pointsLogRepository.sumPointsByMemberBetween(start, end, Role.ACM_MEMBER);

        assertEquals(2, totals.size());
        assertEquals(salma.getId(), totals.get(0).getMemberId());
        assertEquals(60L, totals.get(0).getTotal());
        assertNull(totals.get(0).getCommitteeId());
        assertEquals(nour.getId(), totals.get(1).getMemberId());
        assertEquals(50L, totals.get(1).getTotal());
        assertEquals(technical.getId(), totals.get(1).getCommitteeId());
    }

    private User saveUser(String email, Role role, Committee committee) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setCommittee(committee);
        return userRepository.save(user);
    }

    // created_at is set by Hibernate on insert and is not updatable, so it is backdated with SQL.
    private void saveLog(User member, User assigner, int points, LocalDateTime createdAt) {
        PointsLog log = new PointsLog();
        log.setMember(member);
        log.setAssigner(assigner);
        log.setLastUpdater(assigner);
        log.setPoints(points);
        log.setReason("Task");
        log = pointsLogRepository.saveAndFlush(log);
        entityManager.createNativeQuery("UPDATE points_log SET created_at = :t WHERE id = :id")
                .setParameter("t", createdAt)
                .setParameter("id", log.getId())
                .executeUpdate();
    }
}
