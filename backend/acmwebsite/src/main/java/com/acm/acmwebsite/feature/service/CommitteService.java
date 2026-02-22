package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommiteeRepository;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteService {
    private final CommiteeRepository commiteeRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;

    public CommitteService(CommiteeRepository commiteeRepository, SubscriptionService subscriptionService, EmailService emailService, MessageRepository messageRepository) {
        this.commiteeRepository = commiteeRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
    }

    public void sendCallMessage(SubscripeTo subscripeTo , Long id, Message message) {
        var subscrption = subscriptionService.getAllSubscribersByTopic(subscripeTo,id);
        for (Subscription subscription : subscrption) {
            if(subscription.getStatus()== SubscriptionStatus.ACTIVE)
            {
                emailService.sendEmail(subscription.getEmail(),message);
            }
        }
    }



    public List<Committee> getAll(){
        return commiteeRepository.getAll();
    }

    public Committee getById(Long id){
        return commiteeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Committee save(Committee committee){
        committee= commiteeRepository.save(committee);
        return committee;
    }

    @Transactional
    public CommitteeDto updateCommittee(Long id, CommitteeDto committeeDto) {

        Committee committee = commiteeRepository.findById(id)
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

        committee = commiteeRepository.save(committee);

        return new CommitteeDto(
                committee.getId(),
                committee.getName(),
                committee.getDescription(),
                committee.getLogoUrl(),
                committee.getCallMessage(),
                committee.isOpen(),
                committee.getApplicationFormLink()
        );
    }
    @Transactional
    public void changeCallMessage(Long committeeId, Message message) {

        Committee committee = commiteeRepository.findById(committeeId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Committee not found"));

        message = messageRepository.save(message);

        committee.setCallMessage(message);

        commiteeRepository.save(committee);
    }
    public void delete(Long id){
        commiteeRepository.deleteById(id);
    }





}
