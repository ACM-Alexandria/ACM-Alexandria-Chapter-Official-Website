package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import com.acm.acmwebsite.feature.service.EmailService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    SubscriptionService subscriptionService;

    @Test
    @DisplayName("Send confirmation email to Inactive subscription")
    void shouldSendConfirmationEmail(){

        Subscription subscription = new Subscription("mock@gmail.com", SubscripeTo.COMMITTEE,1234L);
        subscription.setStatus(SubscriptionStatus.PENDING);

        subscriptionService.sendConfirmationEmail(subscription,new Message("hello!"));

        verify(emailService,times(1)).sendEmail(any(),any());
        verify(subscriptionRepository,times(1)).save(any());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should be updated to ACTIVE");
        assertNotNull(subscription.getConfirmedAt(), "ConfirmedAt timestamp should be set");

    }

    @Test
    @DisplayName("send confirmation email to active subscription")
    void shouldNotSendConfirmationEmail(){
        Subscription subscription = new Subscription("mock@gmail.com", SubscripeTo.COMMITTEE,1234L);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionService.sendConfirmationEmail(subscription,new Message("hello!"));
        verify(emailService,never()).sendEmail(any(),any());
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
        String email = "test@example.com";
        SubscripeTo topic = SubscripeTo.COMMITTEE;
        Long id = 1L;
        Subscription sub = new Subscription(email, topic, id);
        sub.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findSubscriptionByEmailAndSubscribeToAndSubscribeToId(email, topic, id))
                .thenReturn(sub);


        subscriptionService.unsubscribeByEmailAndTopic(email, topic, id);

        assertEquals(SubscriptionStatus.UNSUBSCRIBE, sub.getStatus());
        verify(subscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("Get Subscribers: Should return list from repository")
    void shouldReturnSubscribersList() {
        SubscripeTo topic = SubscripeTo.COMMITTEE;
        Long id = 123L;
        Subscription subscription= new Subscription(new Email("mock"),SubscripeTo.COMMITTEE,123L);
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


        Subscription sub= new Subscription(new Email("mock"),SubscripeTo.COMMITTEE,123L);

        subscriptionService.addSubscription(sub);

        verify(subscriptionRepository, times(1)).save(sub);
    }



}