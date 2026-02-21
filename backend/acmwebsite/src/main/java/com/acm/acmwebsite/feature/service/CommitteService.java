package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommiteeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteService {
    private final CommiteeRepository commiteeRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    @Autowired
    public CommitteService(CommiteeRepository commiteeRepository, SubscriptionService subscriptionService,
            EmailService emailService) {
        this.commiteeRepository = commiteeRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
    }

    public void sendCallMessage(SubscripeTo subscripeTo, Long id, Message message) {
        var subscrption = subscriptionService.getAllSubscribersByTopic(subscripeTo, id);
        for (Subscription subscription : subscrption) {
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                // System.out.println("am in sending call message");
                emailService.sendEmail(subscription.getEmail(), message);
            }
        }
    }

    public List<Committee> getAll() {
        return commiteeRepository.getAll();
    }

    public Committee getById(Long id) {
        return commiteeRepository.findWithDetailsById(id).orElse(null);
    }

    @Transactional
    public Committee save(Committee committee) {
        committee = commiteeRepository.save(committee);
        return committee;
    }

    public void delete(Long id) {
        commiteeRepository.deleteById(id);
    }

}
