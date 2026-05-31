package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.commiteedtos.SubscriptionDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.*;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.EmailRepository;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    private final  SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;
    private final CommitteeRepository committeeRepository;
    private final EmailRepository emailRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailService emailService, CommitteeRepository committeeRepository, EmailRepository emailRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
        this.committeeRepository = committeeRepository;
        this.emailRepository = emailRepository;
    }

    public List<Subscription> getAllSubscribersByTopic(SubscripeTo subscripeTo, Long id) {
        return  subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToId(subscripeTo,id);
    }

    public void sendMessageToAllActiveSubscribers(Message message) {
        List<Email> subscriberEmails = subscriptionRepository.findDistinctEmailsByStatus(SubscriptionStatus.ACTIVE);
        for (Email email : subscriberEmails) {
            emailService.sendEmail(email, message);
        }
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

    Optional<Email> getEmailByEmail(String email) {
        return emailService.getObjectByEmail(email);
     }

    public void sendConfirmationEmail(Subscription subscription,Message message) {
        if(subscription.getStatus()== SubscriptionStatus.ACTIVE){
            return;
        }

        emailService.sendEmail(subscription.getEmail(), message);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setConfirmedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

    }
    @Transactional
    public void subscribeToCommittee(Long committeeId, SubscriptionDto subscriptionDto) {

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        Email email = emailRepository.getEmailByEmail(subscriptionDto.getEmail())
                .orElseGet(() -> emailRepository.save(
                        new Email(subscriptionDto.getEmail())
                ));

        Subscription newSubscription = new Subscription(
                email,
                SubscripeTo.COMMITTEE,
                committee.getId()
        );

        subscriptionRepository.save(newSubscription);

        sendConfirmationEmail(
                newSubscription,
                new Message(
                        "Hello",
                        "Your subscription to our mailing list has been confirmed"
                )
        );
    }

}
