package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteeService {
    private final CommitteeRepository committeeRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final CommitteeMapper committeeMapper;

    public CommitteeService(CommitteeRepository committeeRepository, CommitteeBoardRepository committeeBoardRepository,
            CommitteeMapper committeeMapper, SubscriptionService subscriptionService, EmailService emailService,
            MessageRepository messageRepository) {
        this.committeeRepository = committeeRepository;
        this.committeeBoardRepository = committeeBoardRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.committeeMapper = committeeMapper;
    }

    public void sendCallMessage(SubscripeTo subscripeTo, Long id, Message message) {
        var subscriptions = subscriptionService.getAllSubscribersByTopic(subscripeTo, id);
        for (Subscription subscription : subscriptions) {
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                emailService.sendEmail(subscription.getEmail(), message);
            }
        }
    }

    @Transactional
    public void openCommitteeCall(Long id) {
        Committee committee = committeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        if (committee.isOpen()) {
            throw new IllegalStateException("The call is already open!");
        }

        committee.setOpen(true);
        saveCommittee(committee);

        // This is now part of the same atomic operation
        sendCallMessage(SubscripeTo.COMMITTEE, committee.getId(), committee.getCallMessage());
    }

    public List<Committee> getAllCommittees() {
        return committeeRepository.getAll();
    }

    public Committee getCommitteeById(Long id) {
        return committeeRepository.findWithDetailsById(id).orElse(null);
    }

    public Committee saveCommittee(Committee committee) {
        if (committee.getName() == null || committee.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Committee name is required");
        }
        committee = committeeRepository.save(committee);
        return committee;
    }

    @Transactional
    public CommitteeDto updateCommittee(Long id, CommitteeDto committeeDto) {
        if (committeeDto.getName() != null && committeeDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Committee name cannot be empty");
        }

        Committee committee = committeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        committeeMapper.updateCommitteeFromDto(committeeDto, committee);

        committee = committeeRepository.save(committee);

        return committeeMapper.toDto(committee);
    }

    @Transactional
    public void changeCallMessage(Long committeeId, Message message) {

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        message = messageRepository.save(message);

        committee.setCallMessage(message);

        committeeRepository.save(committee);
    }

    public void deleteCommittee(Long id) {
        committeeRepository.deleteById(id);
    }

    public CommitteeBoardMemberDto addCommitteeBoardMember(Long committeeId, CommitteeBoardMemberDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Board member name is required");
        }
        if (dto.getRole() == null || dto.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Board member role is required");
        }

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found with id " + committeeId));

        CommitteeBoard boardEntity = committeeMapper.toBoardEntity(dto);
        boardEntity.setCommittee(committee);

        CommitteeBoard saved = committeeBoardRepository.save(boardEntity);
        return committeeMapper.toBoardDto(saved);
    }

    public CommitteeBoardMemberDto updateCommitteeBoardMember(Long id, CommitteeBoardMemberDto dto) {
        if (dto.getName() != null && dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Board member name cannot be empty");
        }
        if (dto.getRole() != null && dto.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Board member role cannot be empty");
        }

        CommitteeBoard boardEntity = committeeBoardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Committee Board member not found with id " + id));

        committeeMapper.updateBoardEntityFromDto(dto, boardEntity);

        CommitteeBoard saved = committeeBoardRepository.save(boardEntity);
        return committeeMapper.toBoardDto(saved);
    }

    public void deleteCommitteeBoardMember(Long id) {
        if (!committeeBoardRepository.existsById(id)) {
            throw new EntityNotFoundException("Committee Board member not found with id " + id);
        }
        committeeBoardRepository.deleteById(id);
    }

}
