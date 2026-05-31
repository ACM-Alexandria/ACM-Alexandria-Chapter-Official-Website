package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
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
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;
    private final CommitteeRepository committeeRepository;
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailService emailService, 
                               CommitteeRepository committeeRepository, EmailRepository emailRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
        this.committeeRepository = committeeRepository;
        this.emailRepository = emailRepository;
        this.userRepository = userRepository;
    }

    public List<Subscription> getAllSubscribersByTopic(SubscripeTo subscripeTo, Long id) {
        return subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToId(subscripeTo, id);
    }

    public void sendMessageToNewsSubscribers(Message message) {
        List<Subscription> activeSubscriptions = subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE);
        for (Subscription sub : activeSubscriptions) {
            if (sub.getUser() != null) {
                emailService.sendEmail(sub.getUser().getEmail(), message);
            }
        }
    }

    public void addSubscription(Subscription subscription){
        subscriptionRepository.save(subscription);
    }

    public void deleteByTopic(SubscripeTo topic, Long Id) {
        subscriptionRepository.deleteSubscriptionBySubscribeToAndSubscribeToId(topic, Id);
    }

    public void unsubscribeByEmailAndTopic(String email, SubscripeTo topic, Long id) {
        var subscription = subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(email, topic, id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));
        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBE);
        subscriptionRepository.save(subscription);
    }

    public Email addEmail(Email email) {
        return emailService.saveEmail(email);
    }

    Optional<Email> getEmailByEmail(String email) {
        return emailService.getObjectByEmail(email);
    }

    public void sendConfirmationEmail(Subscription subscription, Message message) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        emailService.sendEmail(subscription.getUser().getEmail(), message);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setConfirmedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void subscribeToCommittee(Long committeeId, String userEmail) {

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.COMMITTEE, committeeId)
                .orElseGet(() -> new Subscription(user, SubscripeTo.COMMITTEE, committeeId));

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        subscription.setStatus(SubscriptionStatus.PENDING);
        subscriptionRepository.save(subscription);

        sendConfirmationEmail(
                subscription,
                new Message(
                        "Hello",
                        "Your subscription to " + committee.getName() + " committee mailing list has been confirmed"
                )
        );
    }

    @Transactional
    public void unsubscribeFromCommittee(Long committeeId, String userEmail) {
        Subscription subscription = subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.COMMITTEE, committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBE);
        subscriptionRepository.save(subscription);
    }

    public boolean isSubscribedToCommittee(Long committeeId, String userEmail) {
        return subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.COMMITTEE, committeeId)
                .map(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .orElse(false);
    }

    @Transactional
    public void subscribeToNews(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.NEWS, 0L)
                .orElseGet(() -> new Subscription(user, SubscripeTo.NEWS, 0L));

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        subscription.setStatus(SubscriptionStatus.PENDING);
        subscriptionRepository.save(subscription);

        sendConfirmationEmail(
                subscription,
                new Message(
                        "Hello",
                        "Your subscription to our mailing list has been confirmed"
                )
        );
    }

    @Transactional
    public void unsubscribeFromNews(String userEmail) {
        Subscription subscription = subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.NEWS, 0L)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBE);
        subscriptionRepository.save(subscription);
    }

    public boolean isSubscribedToNews(String userEmail) {
        return subscriptionRepository
                .findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(userEmail, SubscripeTo.NEWS, 0L)
                .map(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .orElse(false);
    }
}
