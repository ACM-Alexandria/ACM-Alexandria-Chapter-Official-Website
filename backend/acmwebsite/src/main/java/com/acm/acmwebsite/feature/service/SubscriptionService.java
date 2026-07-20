package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.Program;
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

    public void sendNewEventNotificationToNewsSubscribers(Event event) {
        List<Subscription> activeSubscriptions = subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE);
        String eventTime = event.getEventTime() != null ? event.getEventTime().toString() : "To be announced";
        String location = event.getLocation() != null && !event.getLocation().isBlank()
                ? event.getLocation()
                : "To be announced";
        for (Subscription sub : activeSubscriptions) {
            if (sub.getUser() != null) {
                emailService.sendNewEventAnnouncementEmail(sub.getUser().getEmail(), event.getName(), eventTime, location, sub.getUser().getName());
            }
        }
    }

    public void sendNewClubNotificationToNewsSubscribers(Club club) {
        List<Subscription> activeSubscriptions = subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE);
        String description = club.getDescription() != null && !club.getDescription().isBlank()
                ? club.getDescription()
                : "No description available";
        for (Subscription sub : activeSubscriptions) {
            if (sub.getUser() != null) {
                emailService.sendNewClubAnnouncementEmail(sub.getUser().getEmail(), club.getName(), description, sub.getUser().getName());
            }
        }
    }

    public void sendNewProgramNotificationToNewsSubscribers(Program program) {
        List<Subscription> activeSubscriptions = subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE);
        String description = program.getDescription() != null && !program.getDescription().isBlank()
                ? program.getDescription()
                : "No description available";
        String startDate = program.getStartDate() != null ? program.getStartDate().toString() : "To be announced";
        for (Subscription sub : activeSubscriptions) {
            if (sub.getUser() != null) {
                emailService.sendNewProgramAnnouncementEmail(sub.getUser().getEmail(), program.getName(), description, startDate, sub.getUser().getName());
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
        return emailRepository.save(email);
    }

    Optional<Email> getEmailByEmail(String email) {
        return emailRepository.getEmailByEmail(email);
    }

    public void sendConfirmationEmail(Subscription subscription, Message message) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        emailService.sendSubscriptionConfirmationEmail(subscription.getUser().getEmail(), message.getSubject(), message.getBody(), subscription.getUser().getName());
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
                        "ACM Website - Confirm Subscription",
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
                        "ACM Website - Confirm Subscription",
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
