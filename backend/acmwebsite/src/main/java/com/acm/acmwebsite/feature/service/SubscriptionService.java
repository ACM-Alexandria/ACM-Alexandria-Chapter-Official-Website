package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.subscriptionStatus;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SubscriptionService {
    private final  SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }


    public void notifyAll(String topic, Message message) {
        var subscriptions=subscriptionRepository.getSubscriptionsByTopic(topic);
        for(Subscription subscription:subscriptions){
            if(subscription.getStatus()== subscriptionStatus.ACTIVE)
            {
                emailService.sendEmail(subscription.getEmail(), message);
            }
        }
    }

    @Transactional
    public void addSubscription(Subscription subscription) {
        if(subscriptionRepository.existsByEmailAndTopic(subscription.getEmail(), subscription.getTopic())){
            return ;
        }
        subscriptionRepository.save(subscription);
    }

    public Subscription getSubscriptionsByEmailAndTopic(String email, String topic) {
        return subscriptionRepository.findByEmailAndTopic(email,topic);
    }


    @Transactional
    public void sendConfirmationEmail(Subscription subscription,Message message) {


        if(!subscriptionRepository.existsByEmailAndTopic(subscription.getEmail(), subscription.getTopic())){
            return;
        }

        if(subscription.getStatus()== subscriptionStatus.ACTIVE){
            return;
        }

        emailService.sendEmail(subscription.getEmail(), message);
        subscription.setStatus(subscriptionStatus.ACTIVE);
        subscription.setConfirmedAt(LocalDate.now());
        subscriptionRepository.save(subscription);

    }

}
