package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.SubscriptionDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.service.CommitteeService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/committee")
public class CommitteeController {
    private CommitteeService committeeService;
    private SubscriptionService subscriptionService;
    private CommitteeMapper committeeMapper;

    @Autowired
    public CommitteeController(CommitteeService committeeService, SubscriptionService subscriptionService,
            CommitteeMapper committeeMapper) {
        this.committeeService = committeeService;
        this.subscriptionService = subscriptionService;
        this.committeeMapper = committeeMapper;
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
        var committee = committeeService.getCommitteeById(id);
        if (committee == null) {
            return ResponseEntity.notFound().build();
        }
        committee.setOpen(false);
        committeeService.saveCommittee(committee);
        return ResponseEntity.ok().build();
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
            @RequestBody SubscriptionDto subscriptionDto) {
        try {
            subscriptionService.subscribeToCommittee(id, subscriptionDto);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
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
