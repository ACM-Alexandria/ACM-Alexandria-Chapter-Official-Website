package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteeService {
    private final CommitteeRepository committeeRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final CommitteeMapper committeeMapper;

    public CommitteeService(CommitteeRepository committeeRepository,CommitteeMapper committeeMapper ,SubscriptionService subscriptionService, EmailService emailService, MessageRepository messageRepository) {
        this.committeeRepository = committeeRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.committeeMapper=committeeMapper;
    }

    public void sendCallMessage(SubscripeTo subscripeTo , Long id, Message message) {
        var subscriptions = subscriptionService.getAllSubscribersByTopic(subscripeTo,id);
        for (Subscription subscription : subscriptions) {
            if(subscription.getStatus()== SubscriptionStatus.ACTIVE)
            {
                emailService.sendEmail(subscription.getEmail(),message);
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



    public List<Committee> getAllCommittees(){
        return committeeRepository.getAll();
    }

    public Committee getCommitteeById(Long id){
        return committeeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Committee saveCommittee(Committee committee){
        committee= committeeRepository.save(committee);
        return committee;
    }

    @Transactional
    public CommitteeDto updateCommittee(Long id, CommitteeDto committeeDto) {

        Committee committee = committeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        // Partial update logic
        if (committeeDto.getName() != null)
            committee.setName(committeeDto.getName());

        if (committeeDto.getDescription() != null)
            committee.setDescription(committeeDto.getDescription());

        if (committeeDto.getLogoUrl() != null)
            committee.setLogoUrl(committeeDto.getLogoUrl());

        if (committeeDto.getApplicationFormLink() != null)
            committee.setApplicationFormLink(committeeDto.getApplicationFormLink());

        committee = committeeRepository.save(committee);

        return committeeMapper.toDto(committee);
    }
    @Transactional
    public void changeCallMessage(Long committeeId, Message message) {

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Committee not found"));

        message = messageRepository.save(message);

        committee.setCallMessage(message);

        committeeRepository.save(committee);
    }
    public void deleteCommittee(Long id){
        committeeRepository.deleteById(id);
    }





}
