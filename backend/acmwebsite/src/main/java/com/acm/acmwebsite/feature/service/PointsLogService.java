package com.acm.acmwebsite.feature.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PointsLogService {

    private final UserRepository userRepository;
    private final PointsLogRepository pointsLogRepository;

    public PointsLogService(UserRepository userRepository, PointsLogRepository pointsLogRepository) {
        this.userRepository = userRepository;
        this.pointsLogRepository = pointsLogRepository;
    }

    public Page<PointsLog> getMemberPointsLog(UUID member_Id, Pageable pageable) {

        return pointsLogRepository.findByMember_Id(member_Id, pageable);
    }

    @Transactional
    public PointsLog updatePointsLog(UUID id, int newPoints, String newReason) {

        PointsLog log = pointsLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PointsLog not found with ID: " + id));

        User member = log.getMember();
        member.addPoints(newPoints - log.getPoints());
        log.setPoints(newPoints);
        log.setReason(newReason);
        return log;
    }

    @Transactional
    public PointsLog addPointsLog(UUID memberId, UUID assignerId, int amount, String reason) {

        User member = userRepository.getReferenceById(memberId);
        User assigner = userRepository.getReferenceById(assignerId);
        PointsLog log = new PointsLog(null, member, assigner, amount, reason, null, null);

        return pointsLogRepository.save(log);
    }
}