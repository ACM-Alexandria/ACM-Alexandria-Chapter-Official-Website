package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.EmailRepository;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @Nested
    @DisplayName("confirmationEmail Tests")
    class ConfirmationEmailTests {

        @Test
        @DisplayName("Send confirmation email to Inactive subscription")
        void shouldSendConfirmationEmail() {
            User user = User.builder().email("dev@example.com").build();
            user.setName("John Doe");
            Subscription subscription = new Subscription(user, SubscripeTo.COMMITTEE, 1234L);
            subscription.setStatus(SubscriptionStatus.PENDING);

            subscriptionService.sendConfirmationEmail(subscription, new Message("hello!", "mock"));

            verify(emailService, times(1)).sendSubscriptionConfirmationEmail(eq("dev@example.com"), eq("hello!"), eq("mock"), eq("John Doe"));
            verify(subscriptionRepository, times(1)).save(any());
            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should be updated to ACTIVE");
            assertNotNull(subscription.getConfirmedAt(), "ConfirmedAt timestamp should be set");
        }

        @Test
        @DisplayName("send confirmation email to active subscription")
        void shouldNotSendConfirmationEmail() {
            User user = User.builder().email("dev@example.com").build();
            Subscription subscription = new Subscription(user, SubscripeTo.COMMITTEE, 1234L);
            subscription.setStatus(SubscriptionStatus.ACTIVE);

            subscriptionService.sendConfirmationEmail(subscription, new Message("hello!", "mock"));
            verify(emailService, never()).sendSubscriptionConfirmationEmail(any(String.class), any(String.class), any(String.class), any(String.class));
            verify(subscriptionRepository, never()).save(any());
            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should not be changed ");
        }
    }

    @Nested
    @DisplayName("topicSubscription Tests")
    class TopicSubscriptionTests {

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
        @DisplayName("Unsubscribe: Not found should throw EntityNotFoundException")
        void unsubscribeByEmailAndTopic_notFound_shouldThrow() {
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId("none@test.com", SubscripeTo.COMMITTEE, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.unsubscribeByEmailAndTopic("none@test.com", SubscripeTo.COMMITTEE, 1L));
        }

        @Test
        @DisplayName("Get Subscribers: Should return list from repository")
        void shouldReturnSubscribersList() {
            SubscripeTo topic = SubscripeTo.COMMITTEE;
            Long id = 123L;
            User user = User.builder().email("mock").build();
            Subscription subscription = new Subscription(user, SubscripeTo.COMMITTEE, 123L);
            List<Subscription> mockList = List.of(subscription, subscription);
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
            Subscription sub = new Subscription(user, SubscripeTo.COMMITTEE, 123L);

            subscriptionService.addSubscription(sub);

            verify(subscriptionRepository, times(1)).save(sub);
        }
    }

    @Nested
    @DisplayName("emailRepository Tests")
    class EmailRepositoryTests {

        @Test
        @DisplayName("addEmail and getEmailByEmail delegation")
        void addAndGetEmail() {
            Email email = new Email();
            email.setEmail("newsletter@acm.org");
            when(emailRepository.save(email)).thenReturn(email);
            when(emailRepository.getEmailByEmail("newsletter@acm.org")).thenReturn(Optional.of(email));

            Email saved = subscriptionService.addEmail(email);
            Optional<Email> fetched = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                    subscriptionService, "getEmailByEmail", "newsletter@acm.org");

            assertSame(email, saved);
            assertNotNull(fetched);
            assertTrue(fetched.isPresent());
            assertEquals("newsletter@acm.org", fetched.get().getEmail());
        }
    }

    @Nested
    @DisplayName("newsNotification Tests")
    class NewsNotificationTests {

        @Test
        @DisplayName("sendNewEventNotificationToNewsSubscribers ignores past events")
        void sendNewEventNotification_pastEvent_shouldIgnore() {
            Event event = new Event();
            event.setEventTime(LocalDateTime.now().minusDays(1));

            subscriptionService.sendNewEventNotificationToNewsSubscribers(event);

            verify(subscriptionRepository, never()).getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(any(), any(), any());
            verify(emailService, never()).sendNewEventAnnouncementEmail(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("sendNewEventNotificationToNewsSubscribers notifies active subscribers for future event")
        void sendNewEventNotification_futureEvent_shouldNotify() {
            Event event = new Event();
            event.setName("AI Summit");
            LocalDateTime eventTime = LocalDateTime.now().plusDays(5);
            event.setEventTime(eventTime);
            event.setLocation("Auditorium 1");

            User u1 = User.builder().email("u1@test.com").name("Alice").build();
            Subscription s1 = new Subscription(u1, SubscripeTo.NEWS, 0L);
            Subscription s2WithNullUser = new Subscription(null, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s1, s2WithNullUser));

            subscriptionService.sendNewEventNotificationToNewsSubscribers(event);

            verify(emailService, times(1)).sendNewEventAnnouncementEmail(
                    eq("u1@test.com"), eq("AI Summit"), eq(eventTime.toString()), eq("Auditorium 1"), eq("Alice"));
        }

        @Test
        @DisplayName("sendNewEventNotificationToNewsSubscribers handles blank location")
        void sendNewEventNotification_nullTimeAndBlankLocation() {
            Event event = new Event();
            event.setName("TBD Event");
            event.setEventTime(null);
            event.setLocation("   ");

            User u = User.builder().email("u@test.com").name("Bob").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewEventNotificationToNewsSubscribers(event);

            verify(emailService).sendNewEventAnnouncementEmail(
                    eq("u@test.com"), eq("TBD Event"), eq("To be announced"), eq("To be announced"), eq("Bob"));
        }

        @Test
        @DisplayName("sendNewEventNotificationToNewsSubscribers handles null location")
        void sendNewEventNotification_nullLocation() {
            Event event = new Event();
            event.setName("TBD Event");
            event.setEventTime(null);
            event.setLocation(null);

            User u = User.builder().email("u@test.com").name("Bob").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewEventNotificationToNewsSubscribers(event);

            verify(emailService).sendNewEventAnnouncementEmail(
                    eq("u@test.com"), eq("TBD Event"), eq("To be announced"), eq("To be announced"), eq("Bob"));
        }

        @Test
        @DisplayName("sendNewClubNotificationToNewsSubscribers notifies active subscribers")
        void sendNewClubNotification_shouldNotify() {
            Club club = new Club();
            club.setName("Robotics");
            club.setDescription("Build robots");

            User u = User.builder().email("robo@test.com").name("Charlie").build();
            Subscription s1 = new Subscription(u, SubscripeTo.NEWS, 0L);
            Subscription s2 = new Subscription(null, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s1, s2));

            subscriptionService.sendNewClubNotificationToNewsSubscribers(club);

            verify(emailService).sendNewClubAnnouncementEmail(
                    eq("robo@test.com"), eq("Robotics"), eq("Build robots"), eq("Charlie"));
        }

        @Test
        @DisplayName("sendNewClubNotificationToNewsSubscribers handles blank description")
        void sendNewClubNotification_blankDescription() {
            Club club = new Club();
            club.setName("Robotics");
            club.setDescription("  ");

            User u = User.builder().email("robo@test.com").name("Charlie").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewClubNotificationToNewsSubscribers(club);

            verify(emailService).sendNewClubAnnouncementEmail(
                    eq("robo@test.com"), eq("Robotics"), eq("No description available"), eq("Charlie"));
        }

        @Test
        @DisplayName("sendNewClubNotificationToNewsSubscribers handles null description")
        void sendNewClubNotification_nullDescription() {
            Club club = new Club();
            club.setName("Robotics");
            club.setDescription(null);

            User u = User.builder().email("robo@test.com").name("Charlie").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewClubNotificationToNewsSubscribers(club);

            verify(emailService).sendNewClubAnnouncementEmail(
                    eq("robo@test.com"), eq("Robotics"), eq("No description available"), eq("Charlie"));
        }

        @Test
        @DisplayName("sendNewProgramNotificationToNewsSubscribers notifies active subscribers")
        void sendNewProgramNotification_shouldNotify() {
            Program program = new Program();
            program.setName("Mentorship");
            program.setDescription("Mentorship program");
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 9, 0);
            program.setStartDate(start);

            User u = User.builder().email("prog@test.com").name("David").build();
            Subscription s1 = new Subscription(u, SubscripeTo.NEWS, 0L);
            Subscription s2 = new Subscription(null, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s1, s2));

            subscriptionService.sendNewProgramNotificationToNewsSubscribers(program);

            verify(emailService).sendNewProgramAnnouncementEmail(
                    eq("prog@test.com"), eq("Mentorship"), eq("Mentorship program"), eq(start.toString()), eq("David"));
        }

        @Test
        @DisplayName("sendNewProgramNotificationToNewsSubscribers handles null description and null startDate")
        void sendNewProgramNotification_nullDescAndNullStart() {
            Program program = new Program();
            program.setName("Mentorship");
            program.setDescription(null);
            program.setStartDate(null);

            User u = User.builder().email("prog@test.com").name("David").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewProgramNotificationToNewsSubscribers(program);

            verify(emailService).sendNewProgramAnnouncementEmail(
                    eq("prog@test.com"), eq("Mentorship"), eq("No description available"), eq("To be announced"), eq("David"));
        }

        @Test
        @DisplayName("sendNewProgramNotificationToNewsSubscribers handles blank description")
        void sendNewProgramNotification_blankDesc() {
            Program program = new Program();
            program.setName("Mentorship");
            program.setDescription("   ");
            program.setStartDate(LocalDateTime.of(2026, 9, 1, 10, 0));

            User u = User.builder().email("prog@test.com").name("David").build();
            Subscription s = new Subscription(u, SubscripeTo.NEWS, 0L);

            when(subscriptionRepository.getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(
                    SubscripeTo.NEWS, 0L, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(s));

            subscriptionService.sendNewProgramNotificationToNewsSubscribers(program);

            verify(emailService).sendNewProgramAnnouncementEmail(
                    eq("prog@test.com"), eq("Mentorship"), eq("No description available"), eq(program.getStartDate().toString()), eq("David"));
        }
    }

    @Nested
    @DisplayName("committeeSubscription Tests")
    class CommitteeSubscriptionTests {

        @Test
        @DisplayName("subscribeToCommittee committee not found should throw EntityNotFoundException")
        void subscribeToCommittee_committeeNotFound_shouldThrow() {
            when(committeeRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.subscribeToCommittee(1L, "user@test.com"));
        }

        @Test
        @DisplayName("subscribeToCommittee user not found should throw EntityNotFoundException")
        void subscribeToCommittee_userNotFound_shouldThrow() {
            Committee committee = new Committee();
            committee.setId(1L);
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.subscribeToCommittee(1L, "user@test.com"));
        }

        @Test
        @DisplayName("subscribeToCommittee already active should return early")
        void subscribeToCommittee_alreadyActive_returnsEarly() {
            Committee committee = new Committee();
            committee.setId(1L);
            User user = User.builder().email("user@test.com").build();
            Subscription sub = new Subscription(user, SubscripeTo.COMMITTEE, 1L);
            sub.setStatus(SubscriptionStatus.ACTIVE);

            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.of(sub));

            subscriptionService.subscribeToCommittee(1L, "user@test.com");

            verify(emailService, never()).sendSubscriptionConfirmationEmail(any(), any(), any(), any());
        }

        @Test
        @DisplayName("subscribeToCommittee new subscription creates and sends confirmation")
        void subscribeToCommittee_newSubscription_succeeds() {
            Committee committee = new Committee();
            committee.setId(1L);
            committee.setName("Web Development");
            User user = User.builder().email("user@test.com").name("Eva").build();

            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.empty());

            subscriptionService.subscribeToCommittee(1L, "user@test.com");

            verify(subscriptionRepository, atLeastOnce()).save(any(Subscription.class));
            verify(emailService).sendSubscriptionConfirmationEmail(
                    eq("user@test.com"),
                    eq("ACM Website - Confirm Subscription"),
                    contains("Web Development"),
                    eq("Eva"));
        }

        @Test
        @DisplayName("unsubscribeFromCommittee not found should throw EntityNotFoundException")
        void unsubscribeFromCommittee_notFound_shouldThrow() {
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.unsubscribeFromCommittee(1L, "user@test.com"));
        }

        @Test
        @DisplayName("unsubscribeFromCommittee success sets status to UNSUBSCRIBE")
        void unsubscribeFromCommittee_success() {
            Subscription sub = new Subscription(new User(), SubscripeTo.COMMITTEE, 1L);
            sub.setStatus(SubscriptionStatus.ACTIVE);
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.of(sub));

            subscriptionService.unsubscribeFromCommittee(1L, "user@test.com");

            assertEquals(SubscriptionStatus.UNSUBSCRIBE, sub.getStatus());
            verify(subscriptionRepository).save(sub);
        }

        @Test
        @DisplayName("isSubscribedToCommittee returns true only when active")
        void isSubscribedToCommittee_tests() {
            Subscription active = new Subscription(new User(), SubscripeTo.COMMITTEE, 1L);
            active.setStatus(SubscriptionStatus.ACTIVE);
            Subscription pending = new Subscription(new User(), SubscripeTo.COMMITTEE, 1L);
            pending.setStatus(SubscriptionStatus.PENDING);

            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "active@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.of(active));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "pending@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.of(pending));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "none@test.com", SubscripeTo.COMMITTEE, 1L)).thenReturn(Optional.empty());

            assertTrue(subscriptionService.isSubscribedToCommittee(1L, "active@test.com"));
            assertFalse(subscriptionService.isSubscribedToCommittee(1L, "pending@test.com"));
            assertFalse(subscriptionService.isSubscribedToCommittee(1L, "none@test.com"));
        }
    }

    @Nested
    @DisplayName("newsSubscription Tests")
    class NewsSubscriptionTests {

        @Test
        @DisplayName("subscribeToNews user not found should throw EntityNotFoundException")
        void subscribeToNews_userNotFound_shouldThrow() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.subscribeToNews("user@test.com"));
        }

        @Test
        @DisplayName("subscribeToNews already active returns early")
        void subscribeToNews_alreadyActive_returnsEarly() {
            User user = User.builder().email("user@test.com").build();
            Subscription sub = new Subscription(user, SubscripeTo.NEWS, 0L);
            sub.setStatus(SubscriptionStatus.ACTIVE);

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.of(sub));

            subscriptionService.subscribeToNews("user@test.com");

            verify(emailService, never()).sendSubscriptionConfirmationEmail(any(), any(), any(), any());
        }

        @Test
        @DisplayName("subscribeToNews new subscription succeeds and confirms")
        void subscribeToNews_newSubscription_succeeds() {
            User user = User.builder().email("user@test.com").name("Frank").build();

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.empty());

            subscriptionService.subscribeToNews("user@test.com");

            verify(subscriptionRepository, atLeastOnce()).save(any(Subscription.class));
            verify(emailService).sendSubscriptionConfirmationEmail(
                    eq("user@test.com"),
                    eq("ACM Website - Confirm Subscription"),
                    contains("our mailing list"),
                    eq("Frank"));
        }

        @Test
        @DisplayName("unsubscribeFromNews not found should throw EntityNotFoundException")
        void unsubscribeFromNews_notFound_shouldThrow() {
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.unsubscribeFromNews("user@test.com"));
        }

        @Test
        @DisplayName("unsubscribeFromNews success sets status to UNSUBSCRIBE")
        void unsubscribeFromNews_success() {
            Subscription sub = new Subscription(new User(), SubscripeTo.NEWS, 0L);
            sub.setStatus(SubscriptionStatus.ACTIVE);

            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "user@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.of(sub));

            subscriptionService.unsubscribeFromNews("user@test.com");

            assertEquals(SubscriptionStatus.UNSUBSCRIBE, sub.getStatus());
            verify(subscriptionRepository).save(sub);
        }

        @Test
        @DisplayName("isSubscribedToNews returns true only when active")
        void isSubscribedToNews_tests() {
            Subscription active = new Subscription(new User(), SubscripeTo.NEWS, 0L);
            active.setStatus(SubscriptionStatus.ACTIVE);
            Subscription pending = new Subscription(new User(), SubscripeTo.NEWS, 0L);
            pending.setStatus(SubscriptionStatus.PENDING);

            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "active@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.of(active));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "pending@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.of(pending));
            when(subscriptionRepository.findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(
                    "none@test.com", SubscripeTo.NEWS, 0L)).thenReturn(Optional.empty());

            assertTrue(subscriptionService.isSubscribedToNews("active@test.com"));
            assertFalse(subscriptionService.isSubscribedToNews("pending@test.com"));
            assertFalse(subscriptionService.isSubscribedToNews("none@test.com"));
        }
    }
}