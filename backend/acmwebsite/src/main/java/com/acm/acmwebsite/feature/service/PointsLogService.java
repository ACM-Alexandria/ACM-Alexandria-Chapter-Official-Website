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

    // `actorEmail` is the signed-in user, who becomes the assigner.
    @Transactional
    public PointsLog addPointsLog(UUID member_Id, String actorEmail, int amount, String reason) {

        User assigner = findActor(actorEmail);
        rejectSelf(member_Id, assigner);
        User member = userRepository.findById(member_Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        member.addPoints(amount);
        PointsLog log = new PointsLog(null, member, assigner, amount, reason, null, null, assigner);

        return pointsLogRepository.save(log);
    }

    // `actorEmail` is the signed-in user, who becomes the last updater.
    @Transactional
    public PointsLog updatePointsLog(Long logId, String actorEmail, int newPoints, String newReason) {

        PointsLog log = pointsLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("PointsLog not found with ID: " + logId));

        User member = log.getMember();
        User updater = findActor(actorEmail);
        rejectSelf(member.getId(), updater);
        member.addPoints(newPoints - log.getPoints());
        log.setPoints(newPoints);
        log.setReason(newReason);
        log.setLastUpdater(updater);
        return log;
    }

    @Transactional
    public void deletePointsLog(Long logId, String actorEmail) {

        PointsLog log = pointsLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Points log not found"));
        User member = log.getMember();
        rejectSelf(member.getId(), findActor(actorEmail));
        member.addPoints(-log.getPoints());
        pointsLogRepository.deleteById(logId);
    }

    private User findActor(String actorEmail) {
        return userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    // Nobody changes their own points, matching the profile panel, which hides the controls on your own profile.
    private void rejectSelf(UUID memberId, User actor) {
        if (actor.getId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't change your own points");
        }
    }
}
