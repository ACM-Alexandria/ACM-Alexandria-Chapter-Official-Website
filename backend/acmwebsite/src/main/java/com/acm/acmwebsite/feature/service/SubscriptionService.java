package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.*;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {
    private final  SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    public List<Subscription> getAllSubscribersByTopic(SubscripeTo subscripeTo,Long id) {
        return  subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToId(subscripeTo,id);
    }



    public void addSubscription(Subscription subscription){
        subscriptionRepository.save(subscription);
    }


    public void deleteByTopic(SubscripeTo topic, Long Id) {
        subscriptionRepository.deleteSubscriptionBySubscribeToAndSubscribeToId(topic,Id);
    }

    public void unsubscribeByEmailAndTopic(String email, SubscripeTo topic,Long id) {
        var subscription = subscriptionRepository.findSubscriptionByEmailAndSubscribeToAndSubscribeToId(email,topic,id);
        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBE);
        subscriptionRepository.save(subscription);
    }
    public Email addEmail(Email email) {
      return      emailService.saveEmail(email);
    }

     public Email getEmailByEmail(String email) {
        return emailService.getObjectByEmail(email);
     }

    @Transactional
    public void sendConfirmationEmail(Subscription subscription,Message message) {
        if(subscription.getStatus()== SubscriptionStatus.ACTIVE){
            return;
        }

        emailService.sendEmail(subscription.getEmail(), message);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setConfirmedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

    }

}
