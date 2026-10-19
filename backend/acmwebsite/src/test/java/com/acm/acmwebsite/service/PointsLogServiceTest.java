package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.service.PointsLogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointsLogServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointsLogRepository pointsLogRepository;

    @InjectMocks
    private PointsLogService pointsLogService;

    private User member;
    private User assigner;
    private UUID memberId;
    private UUID assignerId;
    private UUID logId;
    private PointsLog existingLog;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        assignerId = UUID.randomUUID();
        logId = UUID.randomUUID();

        member = new User();
        member.setId(memberId);
        member.setSeasonPoints(100);

        assigner = new User();
        assigner.setId(assignerId);

        existingLog = new PointsLog();
        existingLog.setId(logId);
        existingLog.setMember(member);
        existingLog.setAssigner(assigner);
        existingLog.setPoints(20);
        existingLog.setReason("Task completed");
    }

    @Test
    void getMemberPointsLog_ShouldReturnPageOfLogs() {
        Page<PointsLog> expectedPage = new PageImpl<>(List.of(existingLog));
        when(pointsLogRepository.findByMember_Id(eq(memberId), any(Pageable.class))).thenReturn(expectedPage);

        Page<PointsLog> result = pointsLogService.getMemberPointsLog(memberId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(pointsLogRepository).findByMember_Id(eq(memberId), any(Pageable.class));
    }

    @Test
    void addPointsLog_ShouldAddPointsToMemberAndSaveLog() {
        when(userRepository.getReferenceById(memberId)).thenReturn(member);
        when(userRepository.getReferenceById(assignerId)).thenReturn(assigner);
        when(pointsLogRepository.save(any(PointsLog.class))).thenAnswer(i -> i.getArguments()[0]);

        PointsLog result = pointsLogService.addPointsLog(memberId, assignerId, 50, "Won hackathon");

        // Points should increase: 100 + 50
        assertEquals(150, member.getSeasonPoints());
        assertEquals(50, result.getPoints());
        assertEquals(member, result.getMember());
        assertEquals(assigner, result.getAssigner());
        verify(pointsLogRepository).save(any(PointsLog.class));
    }

    @Test
    void updatePointsLog_ShouldAdjustMemberPointsAndSave() {
        UUID updaterId = UUID.randomUUID();
        User updater = new User();
        updater.setId(updaterId);

        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(userRepository.getReferenceById(updaterId)).thenReturn(updater);

        // Original log was 20 points. Member currently has 100 points.
        // Changing log to 50 points (an increase of 30 points). Member should end up with 130 points.
        PointsLog result = pointsLogService.updatePointsLog(logId, updaterId, 50, "Updated reason");

        assertEquals(130, member.getSeasonPoints());
        assertEquals(50, result.getPoints());
        assertEquals("Updated reason", result.getReason());
        assertEquals(updater, result.getLastUpdater());
    }

    @Test
    void updatePointsLog_WhenLogNotFound_ShouldThrowException() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            pointsLogService.updatePointsLog(logId, assignerId, 50, "Reason");
        });
    }

    @Test
    void deletePointsLog_ShouldRevertMemberPointsAndDelete() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));

        pointsLogService.deletePointsLog(logId);

        // Member had 100 points, log was 20 points. Reverting should leave 80 points.
        assertEquals(80, member.getSeasonPoints());
        verify(pointsLogRepository).deleteById(logId);
    }

    @Test
    void deletePointsLog_WhenLogNotFound_ShouldThrowException() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            pointsLogService.deletePointsLog(logId);
        });
    }
}