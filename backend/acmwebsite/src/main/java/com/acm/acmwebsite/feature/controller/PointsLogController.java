package com.acm.acmwebsite.feature.controller;

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
@RequestMapping("/api/pointsLog")
public class PointsLogController {
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

    @PostMapping("/add")
    public ResponseEntity<PointsLogDto> addPointsLog(@RequestBody PointsLogDto logDto) {

        PointsLog createdLog = pointsLogService.addPointsLog(
                logDto.getMember(),
                logDto.getAssigner(),
                logDto.getPoints(),
                logDto.getReason());

        return ResponseEntity.status(HttpStatus.CREATED).body(PointsLogDto.convertToDto(createdLog));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PointsLogDto> updatePointsLog(
            @PathVariable("id") UUID logId,
            @RequestBody PointsLogDto logDto) {

        PointsLog updatedLog = pointsLogService.updatePointsLog(
                logId,
                logDto.getPoints(),
                logDto.getReason());

        return ResponseEntity.ok(PointsLogDto.convertToDto(updatedLog));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePointsLog(@PathVariable("id") UUID logId) {

        pointsLogService.deletePointsLog(logId);
        return ResponseEntity.noContent().build();
    }
}