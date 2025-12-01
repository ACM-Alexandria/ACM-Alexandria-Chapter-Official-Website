package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.subscriptionStatus;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {
    private final  SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    public List<Subscription> getAllSubscribersByTopicToken(String topicToken) {
        return subscriptionRepository.getSubscriptionsByTopic(topicToken);
    }



    public void addSubscription(Subscription subscription){
        subscriptionRepository.save(subscription);
    }

    public Subscription findByEmailAndTopic(String email,String topic) {
        return subscriptionRepository.findByEmailAndTopic(email,topic);
    }

    public Subscription getSubscriptionsByEmailAndTopic(String email, String topic) {
        return subscriptionRepository.findByEmailAndTopic(email,topic);
    }
    public void deleteByTopic(String topic) {
        subscriptionRepository.deleteSubscriptionByTopic(topic);
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
