package com.acm.acmwebsite.feature.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public PointsLog addPointsLog(UUID member_Id, UUID assigner_Id, int amount, String reason) {

        User member = userRepository.getReferenceById(member_Id);
        User assigner = userRepository.getReferenceById(assigner_Id);
        member.addPoints(amount);
        PointsLog log = new PointsLog(null, member, assigner, amount, reason, null, null, assigner);

        return pointsLogRepository.save(log);
    }

    @Transactional
    public PointsLog updatePointsLog(UUID logId, UUID updaterID ,int newPoints, String newReason) {

        PointsLog log = pointsLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("PointsLog not found with ID: " + logId));

        User member = log.getMember();
        User updater = userRepository.getReferenceById(updaterID);
        member.addPoints(newPoints - log.getPoints());
        log.setPoints(newPoints);
        log.setReason(newReason);
        log.setLastUpdater(updater);
        return log;
    }

    @Transactional
    public void deletePointsLog(UUID logId) {

        PointsLog log = pointsLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Points log not found"));
        User member = log.getMember();
        member.addPoints(-log.getPoints());
        pointsLogRepository.deleteById(logId);
    }
}