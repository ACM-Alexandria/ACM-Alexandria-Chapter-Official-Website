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
import java.util.concurrent.ThreadLocalRandom;

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

    private static final String ASSIGNER_EMAIL = "assigner@test.com";
    private static final String MEMBER_EMAIL = "member@test.com";

    private User member;
    private User assigner;
    private UUID memberId;
    private UUID assignerId;
    private Long logId;
    private PointsLog existingLog;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        assignerId = UUID.randomUUID();
        logId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

        member = new User();
        member.setId(memberId);
        member.setSeasonPoints(100);

        assigner = new User();
        assigner.setId(assignerId);
        assigner.setEmail(ASSIGNER_EMAIL);

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
        when(userRepository.findByEmail(ASSIGNER_EMAIL)).thenReturn(Optional.of(assigner));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(pointsLogRepository.save(any(PointsLog.class))).thenAnswer(i -> i.getArguments()[0]);

        PointsLog result = pointsLogService.addPointsLog(memberId, ASSIGNER_EMAIL, 50, "Won hackathon");

        // Points should increase: 100 + 50
        assertEquals(150, member.getSeasonPoints());
        assertEquals(50, result.getPoints());
        assertEquals(member, result.getMember());
        assertEquals(assigner, result.getAssigner());
        assertEquals(assigner, result.getLastUpdater());
        verify(pointsLogRepository).save(any(PointsLog.class));
    }

    @Test
    void addPointsLog_ToSelf_ShouldBeForbiddenAndChangeNothing() {
        member.setEmail(MEMBER_EMAIL);
        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pointsLogService.addPointsLog(memberId, MEMBER_EMAIL, 50, "Self award"));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals(100, member.getSeasonPoints());
        verify(pointsLogRepository, never()).save(any(PointsLog.class));
    }

    @Test
    void addPointsLog_WhenMemberNotFound_ShouldThrowNotFound() {
        when(userRepository.findByEmail(ASSIGNER_EMAIL)).thenReturn(Optional.of(assigner));
        when(userRepository.findById(memberId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pointsLogService.addPointsLog(memberId, ASSIGNER_EMAIL, 50, "Reason"));

        assertEquals(404, ex.getStatusCode().value());
        verify(pointsLogRepository, never()).save(any(PointsLog.class));
    }

    @Test
    void addPointsLog_WhenActorUnknown_ShouldThrowUnauthorized() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pointsLogService.addPointsLog(memberId, "ghost@test.com", 50, "Reason"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void updatePointsLog_ShouldAdjustMemberPointsAndSave() {
        User updater = new User();
        updater.setId(UUID.randomUUID());
        updater.setEmail("updater@test.com");

        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(userRepository.findByEmail("updater@test.com")).thenReturn(Optional.of(updater));

        // Original log was 20 points. Member currently has 100 points.
        // Changing log to 50 points (an increase of 30 points). Member should end up with 130 points.
        PointsLog result = pointsLogService.updatePointsLog(logId, "updater@test.com", 50, "Updated reason");

        assertEquals(130, member.getSeasonPoints());
        assertEquals(50, result.getPoints());
        assertEquals("Updated reason", result.getReason());
        assertEquals(updater, result.getLastUpdater());
    }

    @Test
    void updatePointsLog_OnOwnLog_ShouldBeForbiddenAndChangeNothing() {
        member.setEmail(MEMBER_EMAIL);
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));

        assertThrows(ResponseStatusException.class,
                () -> pointsLogService.updatePointsLog(logId, MEMBER_EMAIL, 500, "Bumped"));

        assertEquals(100, member.getSeasonPoints());
        assertEquals(20, existingLog.getPoints());
    }

    @Test
    void updatePointsLog_WhenLogNotFound_ShouldThrowException() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            pointsLogService.updatePointsLog(logId, ASSIGNER_EMAIL, 50, "Reason");
        });
    }

    @Test
    void deletePointsLog_ShouldRevertMemberPointsAndDelete() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(userRepository.findByEmail(ASSIGNER_EMAIL)).thenReturn(Optional.of(assigner));

        pointsLogService.deletePointsLog(logId, ASSIGNER_EMAIL);

        // Member had 100 points, log was 20 points. Reverting should leave 80 points.
        assertEquals(80, member.getSeasonPoints());
        verify(pointsLogRepository).deleteById(logId);
    }

    @Test
    void deletePointsLog_OnOwnLog_ShouldBeForbidden() {
        member.setEmail(MEMBER_EMAIL);
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));

        assertThrows(ResponseStatusException.class, () -> pointsLogService.deletePointsLog(logId, MEMBER_EMAIL));

        assertEquals(100, member.getSeasonPoints());
        verify(pointsLogRepository, never()).deleteById(any());
    }

    @Test
    void deletePointsLog_WhenLogNotFound_ShouldThrowException() {
        when(pointsLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            pointsLogService.deletePointsLog(logId, ASSIGNER_EMAIL);
        });
    }
}