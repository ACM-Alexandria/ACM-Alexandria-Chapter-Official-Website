package com.acm.acmwebsite.feature.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.dto.PointsLogDto;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.service.PointsLogService;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD', 'ACM_MEMBER')")
@RequestMapping("/api/pointsLog")
public class PointsLogController {

    // Who may change points: the same people who can open a member's profile panel in the
    // admin page or the HR dashboard (see UserController#adminGetUserProfile).
    private static final String CAN_MANAGE_POINTS =
            "hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')"
                    + " or @authorizationServiceImpl.isHrCommitteeMember(authentication)";

    private final PointsLogService pointsLogService;

    public PointsLogController(PointsLogService pointsLogService) {
        this.pointsLogService = pointsLogService;
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<PointsLogDto>> getMemberLogs(@PathVariable("id") UUID memberId,
                                                            @PageableDefault(size = 5, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PointsLog> logs = pointsLogService.getMemberPointsLog(memberId, pageable);
        Page<PointsLogDto> logsDto = logs.map(PointsLogDto::convertToDto);
        return ResponseEntity.ok(logsDto);
    }

    // The assigner is always the signed-in user; `assigner` in the body is ignored.
    @PostMapping("/add")
    @PreAuthorize(CAN_MANAGE_POINTS)
    public ResponseEntity<PointsLogDto> addPointsLog(@RequestBody PointsLogDto logDto, Authentication authentication) {

        PointsLog createdLog = pointsLogService.addPointsLog(
                logDto.getMember(),
                authentication.getName(),
                logDto.getPoints(),
                logDto.getReason());

        return ResponseEntity.status(HttpStatus.CREATED).body(PointsLogDto.convertToDto(createdLog));
    }

    // The updater is always the signed-in user; `lastUpdater` in the body is ignored.
    @PutMapping("/update/{id}")
    @PreAuthorize(CAN_MANAGE_POINTS)
    public ResponseEntity<PointsLogDto> updatePointsLog(
            @PathVariable("id") Long logId,
            @RequestBody PointsLogDto logDto,
            Authentication authentication) {

        PointsLog updatedLog = pointsLogService.updatePointsLog(
                logId,
                authentication.getName(),
                logDto.getPoints(),
                logDto.getReason());

        return ResponseEntity.ok(PointsLogDto.convertToDto(updatedLog));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize(CAN_MANAGE_POINTS)
    public ResponseEntity<Void> deletePointsLog(@PathVariable("id") Long logId, Authentication authentication) {

        pointsLogService.deletePointsLog(logId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
