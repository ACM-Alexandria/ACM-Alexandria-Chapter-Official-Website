package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import com.acm.acmwebsite.feature.repository.EmailRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    EmailService emailService;

    @Mock
    UserRepository userRepository;

    @Mock
    EmailRepository emailRepository;

    @Mock
    CommitteeRepository committeeRepository;

    @InjectMocks
    SubscriptionService subscriptionService;

    @Test
    @DisplayName("Send confirmation email to Inactive subscription")
    void shouldSendConfirmationEmail(){
        User user = User.builder().email("dev@example.com").build();
        user.setName("John Doe");
        Subscription subscription = new Subscription(user, SubscripeTo.COMMITTEE,1234L);
        subscription.setStatus(SubscriptionStatus.PENDING);

        subscriptionService.sendConfirmationEmail(subscription,new Message("hello!","mock"));

        verify(emailService,times(1)).sendSubscriptionConfirmationEmail(eq("dev@example.com"), eq("hello!"), eq("mock"), eq("John Doe"));
        verify(subscriptionRepository,times(1)).save(any());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should be updated to ACTIVE");
        assertNotNull(subscription.getConfirmedAt(), "ConfirmedAt timestamp should be set");

    }

    @Test
    @DisplayName("send confirmation email to active subscription")
    void shouldNotSendConfirmationEmail(){
        User user = User.builder().email("dev@example.com").build();
        Subscription subscription = new Subscription(user, SubscripeTo.COMMITTEE,1234L);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionService.sendConfirmationEmail(subscription,new Message("hello!","mock"));
        verify(emailService,never()).sendSubscriptionConfirmationEmail(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(subscriptionRepository,never()).save(any());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should not be changed ");
    }

    @Test
    @DisplayName("Should call repository to delete subscription by topic and ID")
    void shouldDeleteByTopicAndId() {

        SubscripeTo topic = SubscripeTo.COMMITTEE;
        Long targetId = 2344L;

        subscriptionService.deleteByTopic(topic, targetId);

        verify(subscriptionRepository, times(1))
                .deleteSubscriptionBySubscribeToAndSubscribeToId(topic, targetId);
    }


    @Test
    @DisplayName("Unsubscribe: Successfully change status to UNSUBSCRIBE")
    void shouldUnsubscribeSuccessfully() {
        User user = User.builder().email("dev@example.com").build();
        SubscripeTo topic = SubscripeTo.COMMITTEE;
        Long id = 1L;
        Subscription sub = new Subscription(user, topic, id);
        sub.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(user.getEmail(), topic, id))
                .thenReturn(Optional.of(sub));


        subscriptionService.unsubscribeByEmailAndTopic(user.getEmail(), topic, id);

        assertEquals(SubscriptionStatus.UNSUBSCRIBE, sub.getStatus());
        verify(subscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("Get Subscribers: Should return list from repository")
    void shouldReturnSubscribersList() {
        SubscripeTo topic = SubscripeTo.COMMITTEE;
        Long id = 123L;
        User user = User.builder().email("mock").build();
        Subscription subscription= new Subscription(user,SubscripeTo.COMMITTEE,123L);
        List<Subscription> mockList = List.of(subscription,subscription);
        when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToId(topic, id))
                .thenReturn(mockList);


        List<Subscription> result = subscriptionService.getAllSubscribersByTopic(topic, id);


        assertEquals(2, result.size());
        verify(subscriptionRepository).getSubscriptionsBySubscribeToAndSubscribeToId(topic, id);
    }

    @Test
    @DisplayName("Add Subscription: Should call save on repository")
    void shouldCallSaveOnRepository() {

        User user = User.builder().email("mock").build();
        Subscription sub= new Subscription(user,SubscripeTo.COMMITTEE,123L);

        subscriptionService.addSubscription(sub);

        verify(subscriptionRepository, times(1)).save(sub);
    }

}