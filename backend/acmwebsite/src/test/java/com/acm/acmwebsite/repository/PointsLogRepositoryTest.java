package com.acm.acmwebsite.repository;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class PointsLogRepositoryTest {

    @Autowired
    private PointsLogRepository pointsLogRepository;

    @Autowired
    private UserRepository userRepository;

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
}