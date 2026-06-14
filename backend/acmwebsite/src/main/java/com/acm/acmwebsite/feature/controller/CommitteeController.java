package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.CommitteeCallResponseDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.service.CommitteeService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.acm.acmwebsite.feature.service.CommitteeRegistrationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/committee")
public class CommitteeController {
    private final CommitteeService committeeService;
    private final SubscriptionService subscriptionService;
    private final CommitteeMapper committeeMapper;
    private final CommitteeRegistrationService committeeRegistrationService;

    @Autowired
    public CommitteeController(CommitteeService committeeService, SubscriptionService subscriptionService,
            CommitteeMapper committeeMapper, CommitteeRegistrationService committeeRegistrationService) {
        this.committeeService = committeeService;
        this.subscriptionService = subscriptionService;
        this.committeeMapper = committeeMapper;
        this.committeeRegistrationService = committeeRegistrationService;
    }

    @GetMapping
    List<CommitteeDto> getAllCommittees() {
        return committeeService.getAllCommittees().stream().map(committeeMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommitteeDto> findCommitteeById(@PathVariable Long id) {
        var committee = committeeService.getCommitteeById(id);
        if (committee == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(committeeMapper.toDto(committee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCommittee(@PathVariable Long id,
            @RequestBody CommitteeDto committeeDto) {
        try {
            CommitteeDto updated = committeeService.updateCommittee(id, committeeDto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCommittee(@RequestBody CommitteeDto committeeDto) {
        Committee newCommittee = committeeMapper.toEntity(committeeDto);

        try {
            committeeService.saveCommittee(newCommittee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "this committee already exists"));
        }

        return ResponseEntity.ok(committeeMapper.toDto(newCommittee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommittee(@PathVariable Long id) {
        var committee = committeeService.getCommitteeById(id);
        if (committee == null) {
            return ResponseEntity.notFound().build();
        }
        committeeService.deleteCommittee(id);
        subscriptionService.deleteByTopic(SubscripeTo.COMMITTEE, committee.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/open-call")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> openCommitteeCall(@PathVariable Long id) {
        try {
            committeeService.openCommitteeCall(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/close-call")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> closeCall(@PathVariable Long id) {
        try {
            committeeService.closeCommitteeCall(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Member Registration / Application Endpoints ---

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getCommitteeQuestions(@PathVariable("id") Long committeeId) {
        return ResponseEntity.ok(committeeRegistrationService.getQuestions(committeeId));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> registerUserForCommittee(
            @PathVariable("id") Long committeeId,
            @RequestBody RegistrationRequestDto request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        committeeRegistrationService.registerUser(request.getUserId(), committeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/is-registered")
    public ResponseEntity<Map<String, Boolean>> isRegisteredForCommittee(
            @PathVariable("id") Long committeeId,
            @RequestParam("userId") java.util.UUID userId) {
        boolean registered = committeeRegistrationService.isUserRegistered(userId, committeeId);
        return ResponseEntity.ok(Map.of("registered", registered));
    }

    // --- Admin Question Management Endpoints ---

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> createCommitteeQuestion(
            @PathVariable("id") Long committeeId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(committeeRegistrationService.createQuestion(committeeId, request));
    }

    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> updateCommitteeQuestion(
            @PathVariable("id") Long committeeId,
            @PathVariable Long questionId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.ok(committeeRegistrationService.updateQuestion(committeeId, questionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommitteeQuestion(
            @PathVariable("id") Long committeeId,
            @PathVariable Long questionId) {
        committeeRegistrationService.deleteQuestion(committeeId, questionId);
        return ResponseEntity.noContent().build();
    }

    // --- Admin Call History & Google Sheets Sync Endpoints ---

    @GetMapping("/{id}/calls")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommitteeCallResponseDto>> getCommitteeCalls(@PathVariable("id") Long committeeId) {
        return ResponseEntity.ok(committeeRegistrationService.getCallsForCommittee(committeeId));
    }

    @GetMapping("/calls/{callId}/registrations/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> getCallRegistrationAnalysis(@PathVariable Long callId) {
        return ResponseEntity.ok(committeeRegistrationService.getRegistrationAnalysis(callId));
    }

    @PostMapping("/calls/{callId}/registrations/sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> syncCallRegistrationsSheet(@PathVariable Long callId) {
        return ResponseEntity.ok(committeeRegistrationService.syncRegistrationsSheet(callId));
    }

    @PostMapping("{id}/change-message")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeCallMessage(@PathVariable Long id,
            @RequestBody Message message) {
        try {
            committeeService.changeCallMessage(id, message);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribeToCommittee(@PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            subscriptionService.subscribeToCommittee(id, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<?> unsubscribeFromCommittee(@PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            subscriptionService.unsubscribeFromCommittee(id, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/subscription-status")
    public ResponseEntity<?> getCommitteeSubscriptionStatus(@PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.ok(Map.of("subscribed", false));
        }
        try {
            boolean isSubscribed = subscriptionService.isSubscribedToCommittee(id, authentication.getName());
            return ResponseEntity.ok(Map.of("subscribed", isSubscribed));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("subscribed", false));
        }
    }

    @PostMapping("/{committeeId}/board-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addCommitteeBoardMember(
            @PathVariable Long committeeId,
            @RequestBody CommitteeBoardMemberDto dto) {
        try {
            CommitteeBoardMemberDto created = committeeService.addCommitteeBoardMember(committeeId, dto);
            return ResponseEntity.ok(created);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/board-members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCommitteeBoardMember(
            @PathVariable Long id,
            @RequestBody CommitteeBoardMemberDto dto) {
        try {
            CommitteeBoardMemberDto updated = committeeService.updateCommitteeBoardMember(id, dto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/board-members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCommitteeBoardMember(@PathVariable Long id) {
        try {
            committeeService.deleteCommitteeBoardMember(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
