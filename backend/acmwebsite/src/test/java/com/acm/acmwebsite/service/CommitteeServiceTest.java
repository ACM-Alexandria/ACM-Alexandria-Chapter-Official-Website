package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.service.CommitteeService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeCallRepository;
import com.acm.acmwebsite.feature.entity.CommitteeCall;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommitteeServiceTest {

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    CommitteeService committeService;
    @Mock
    CommitteeRepository commiteeRepository;

    @Mock
    CommitteeBoardRepository committeeBoardRepository;

    @Mock
    CommitteeMapper committeeMapper;

    @Mock
    SubscriptionService subscriptionService;

    @Mock
    EmailService emailService;

    @Mock
    CommitteeCallRepository committeeCallRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    com.acm.acmwebsite.User_Authentication.repository.UserRepository userRepository;




    @Test
    @DisplayName("send call message successfully")
    void sendCallMessageSuccessfully()
    {

        long targetId = 2344L;
        Message testMessage = new Message("Hello!","mock");
        User user = User.builder().email("dev@example.com").build();
        Subscription activeSub = new Subscription(user, SubscripeTo.COMMITTEE, targetId);
        activeSub.setStatus(SubscriptionStatus.ACTIVE);
        activeSub.setId(targetId);
        activeSub.setUser(user);
        List<Subscription> subscriptions = List.of(activeSub);


        when(subscriptionService.getAllSubscribersByTopic(eq(SubscripeTo.COMMITTEE), eq(targetId)))
                .thenReturn(subscriptions);


        committeService.sendCallMessage(SubscripeTo.COMMITTEE, targetId, testMessage);

        verify(emailService, times(1)).sendCommitteeCallEmail(user.getEmail(), testMessage.getSubject(), testMessage.getBody(),user.getName());

    }

    @Test
    @DisplayName("send call message with wrong subscribeTo param")
    void snedCallMessageWithWrongSubscribeTO(){
        long targetId=2233L;
        User user = User.builder().email("dev@example.com").build();
        Subscription activeSub = new Subscription(user, SubscripeTo.COMMITTEE, targetId);
        activeSub.setStatus(SubscriptionStatus.ACTIVE);
        lenient().when(subscriptionService.getAllSubscribersByTopic(SubscripeTo.COMMITTEE,targetId)).thenReturn(
               List.of(activeSub)
        );


        committeService.sendCallMessage(SubscripeTo.NEWS,targetId,new Message("message !","mock"));

        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any(),any(String.class));
    }

    @Test
    @DisplayName("send call message with wrong id ")
    void snedCallMessageWithWrongId(){
        //given
        long targetId = 2344L;
        long wrongId=3344L;
        User user = User.builder().email("dev@example.com").build();
        Subscription activeSub = new Subscription(user, SubscripeTo.COMMITTEE, targetId);
        activeSub.setStatus(SubscriptionStatus.ACTIVE);
        lenient().when(subscriptionService.getAllSubscribersByTopic(SubscripeTo.COMMITTEE,targetId)).thenReturn(
                List.of(activeSub)
        );

        committeService.sendCallMessage(SubscripeTo.COMMITTEE,wrongId,new Message("message !","mock"));

        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any(),any(String.class));

    }
    private Committee createDummyCommittee(){
        Committee c= new Committee();
        c.setName("commiteee");
        c.setId(1);
        return c;
    }


    @Test
    @DisplayName("Should NOT send email when subscriber status is INACTIVE")
    void shouldNotSendEmailWhenStatusIsInactive() {
        // Arrange
        long targetId = 2344L;
        Message testMessage = new Message("Important Update","mock");
        User user = User.builder().email("dev@example.com").build();
        Subscription inactiveSub = new Subscription(user, SubscripeTo.COMMITTEE, targetId);
        inactiveSub.setStatus(SubscriptionStatus.PENDING); // Set to INACTIVE
        inactiveSub.setUser(User.builder().email("user@example.com").build());

        when(subscriptionService.getAllSubscribersByTopic(SubscripeTo.COMMITTEE, targetId))
                .thenReturn(List.of(inactiveSub));

        // Act
        committeService.sendCallMessage(SubscripeTo.COMMITTEE, targetId, testMessage);

        // Assert
        // If your 'if' check is working, this email should never be sent
        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any(),any(String.class));
    }

    @Test
    @DisplayName("return ALL committees ")
    void shouldReturnAllCommittees(){
        Committee c1 = createDummyCommittee();
        Committee c2 =createDummyCommittee();
        c2.setName("c2");
        List<Committee> committees= List.of(c1,c2);

        when(commiteeRepository.getAll()).thenReturn(
                committees
        );


        var list=committeService.getAllCommittees();

        assertEquals(2,list.size());
        verify(commiteeRepository,times(1)).getAll();

    }

    @Test
    @DisplayName("Should return committee when ID exists")
    void shouldReturnCommitteeById() {
        // Arrange
        Committee committee = createDummyCommittee();
        committee.setName("HR");
        when(commiteeRepository.findWithDetailsById(1L)).thenReturn(Optional.of(committee));

        // Act
        Committee result = committeService.getCommitteeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("HR", result.getName());
    }

    @Test
    @DisplayName("Should return null when ID does not exist")
    void shouldReturnNullWhenIdNotFound() {
        // Arrange
        when(commiteeRepository.findWithDetailsById(99L)).thenReturn(Optional.empty());

        // Act
        Committee result = committeService.getCommitteeById(99L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("addCommitteeBoardMember works successfully")
    void addCommitteeBoardMemberSuccessfully() {
        Committee committee = createDummyCommittee();
        com.acm.acmwebsite.User_Authentication.entity.User dummyUser = new com.acm.acmwebsite.User_Authentication.entity.User();
        dummyUser.setId(java.util.UUID.randomUUID());
        java.util.UUID userId = dummyUser.getId();

        CommitteeBoard boardEntity = new CommitteeBoard(1L, "President", 1, new Committee(), dummyUser);
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "President", 1, userId);
        CommitteeBoard savedEntity = new CommitteeBoard(1L, "President", 1, new Committee(), dummyUser);
        CommitteeBoardMemberDto savedDto = new CommitteeBoardMemberDto(1L, "President", 1, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeMapper.toBoardEntity(dto)).thenReturn(boardEntity);
        when(committeeBoardRepository.save(boardEntity)).thenReturn(savedEntity);
        when(committeeMapper.toBoardDto(savedEntity)).thenReturn(savedDto);

        CommitteeBoardMemberDto result = committeService.addCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("updateCommitteeBoardMember updates existing entity fields")
    void updateCommitteeBoardMemberSuccessfully() {
        com.acm.acmwebsite.User_Authentication.entity.User dummyUser = new com.acm.acmwebsite.User_Authentication.entity.User();
        dummyUser.setId(java.util.UUID.randomUUID());
        java.util.UUID userId = dummyUser.getId();
        
        CommitteeBoard boardEntity = new CommitteeBoard(1L, "President", 1, new Committee(), dummyUser);
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "Vice", 2, userId);
        CommitteeBoard savedEntity = new CommitteeBoard(1L, "Vice", 2, new Committee(), dummyUser);
        CommitteeBoardMemberDto savedDto = new CommitteeBoardMemberDto(1L, "Vice", 2, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(savedEntity);
        when(committeeMapper.toBoardDto(savedEntity)).thenReturn(savedDto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Vice", result.getRole());
    }

    @Test
    @DisplayName("deleteCommitteeBoardMember deletes successfully when exists")
    void deleteCommitteeBoardMemberSuccessfully() {
        when(committeeBoardRepository.existsById(1L)).thenReturn(true);

        committeService.deleteCommitteeBoardMember(1L);

        verify(committeeBoardRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteCommitteeBoardMember throws exception when not found")
    void deleteCommitteeBoardMemberThrowsWhenNotFound() {
        when(committeeBoardRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> committeService.deleteCommitteeBoardMember(99L));
    }

    @Test
    @DisplayName("openCommitteeCall successfully creates call and sets open to true")
    void openCommitteeCallSuccessfully() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(new Message("Subject", "Body"));

        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);
        when(subscriptionService.getAllSubscribersByTopic(any(), any())).thenReturn(List.of());

        committeService.openCommitteeCall(1L, true);

        assertTrue(committee.isOpen());
        verify(committeeCallRepository, times(1)).save(argThat(call -> call.getAnnouncementSentAt() != null));
        verify(subscriptionService).getAllSubscribersByTopic(SubscripeTo.COMMITTEE, 1L);
    }

    @Test
    @DisplayName("openCommitteeCall while emails are locked opens the call without emailing subscribers")
    void openCommitteeCallWhileEmailsLocked() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(new Message("Subject", "Body"));

        when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);

        committeService.openCommitteeCall(1L, true);

        assertTrue(committee.isOpen());
        // Nothing went out, so the call must still be announceable later
        verify(committeeCallRepository, times(1)).save(argThat(call -> call.getAnnouncementSentAt() == null));
        verify(subscriptionService, never()).getAllSubscribersByTopic(any(), any());
        verify(emailService, never()).sendCommitteeCallEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("openCommitteeCall rejects an empty call message when the email goes out now")
    void openCommitteeCallRejectsEmptyMessage() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(null);

        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalArgumentException.class, () -> committeService.openCommitteeCall(1L, true));

        assertFalse(committee.isOpen());
        verify(commiteeRepository, never()).save(any(Committee.class));
        verify(committeeCallRepository, never()).save(any(CommitteeCall.class));
        verify(subscriptionService, never()).getAllSubscribersByTopic(any(), any());
    }

    @Test
    @DisplayName("openCommitteeCall while emails are locked opens the call even with an empty message")
    void openCommitteeCallWhileLockedAllowsEmptyMessage() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(null);

        when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);

        committeService.openCommitteeCall(1L, true);

        // Nothing was sent, so the message is checked later by the manual resend
        assertTrue(committee.isOpen());
        verify(committeeCallRepository, times(1)).save(argThat(call -> call.getAnnouncementSentAt() == null));
        verify(subscriptionService, never()).getAllSubscribersByTopic(any(), any());
    }

    @Test
    @DisplayName("openCommitteeCall throws exception when already open")
    void openCommitteeCallThrowsWhenAlreadyOpen() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalStateException.class, () -> committeService.openCommitteeCall(1L, true));
    }

    @Test
    @DisplayName("closeCommitteeCall successfully closes call and sets open to false")
    void closeCommitteeCallSuccessfully() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);
        CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));

        committeService.closeCommitteeCall(1L);

        assertFalse(committee.isOpen());
        assertNotNull(activeCall.getClosedAt());
        verify(committeeCallRepository, times(1)).save(activeCall);
    }

    @Test
    @DisplayName("closeCommitteeCall throws exception when already closed")
    void closeCommitteeCallThrowsWhenAlreadyClosed() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalStateException.class, () -> committeService.closeCommitteeCall(1L));
    }


    @Test
    @DisplayName("openCommitteeCall with sendAnnouncement false opens the call without emailing subscribers")
    void openCommitteeCallWithoutAnnouncement() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(new Message("Subject", "Body"));

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);

        committeService.openCommitteeCall(1L, false);

        assertTrue(committee.isOpen());
        verify(committeeCallRepository, times(1)).save(any(CommitteeCall.class));
        verify(subscriptionService, never()).getAllSubscribersByTopic(any(), any());
    }

    @Test
    @DisplayName("announceCommitteeCall emails subscribers when the call is open")
    void announceCommitteeCallWhenOpen() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);
        committee.setCallMessage(new Message("Subject", "Body"));

        CommitteeCall call = CommitteeCall.builder().id(5L).committee(committee).build();
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(call));
        when(committeeCallRepository.claimAnnouncement(eq(5L), any())).thenReturn(1);
        when(subscriptionService.getAllSubscribersByTopic(any(), any())).thenReturn(List.of());

        committeService.announceCommitteeCall(1L, false);

        verify(subscriptionService).getAllSubscribersByTopic(SubscripeTo.COMMITTEE, committee.getId());
    }

    @Test
    @DisplayName("announceCommitteeCall throws exception when the call is closed")
    void announceCommitteeCallThrowsWhenClosed() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalStateException.class, () -> committeService.announceCommitteeCall(1L, false));
    }

    @Test
    @DisplayName("announceCommitteeCall refuses a second send for the same call without force")
    void announceCommitteeCallAlreadySent() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);
        committee.setCallMessage(new Message("Subject", "Body"));
        CommitteeCall call = CommitteeCall.builder().id(5L).committee(committee).build();

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(call));
        when(committeeCallRepository.claimAnnouncement(eq(5L), any())).thenReturn(0);

        assertThrows(AnnouncementAlreadySentException.class, () -> committeService.announceCommitteeCall(1L, false));

        verify(subscriptionService, never()).getAllSubscribersByTopic(any(), any());
    }

    @Test
    @DisplayName("announceCommitteeCall with force resends an already sent call and updates the sent time")
    void announceCommitteeCallForceResends() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);
        committee.setCallMessage(new Message("Subject", "Body"));
        LocalDateTime firstSentAt = LocalDateTime.now().minusDays(1);
        CommitteeCall call = CommitteeCall.builder().id(5L).committee(committee).announcementSentAt(firstSentAt).build();

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(call));
        when(subscriptionService.getAllSubscribersByTopic(any(), any())).thenReturn(List.of());

        committeService.announceCommitteeCall(1L, true);

        // Force skips the claim, so an earlier send doesn't block it
        verify(committeeCallRepository, never()).claimAnnouncement(any(), any());
        assertTrue(call.getAnnouncementSentAt().isAfter(firstSentAt));
        verify(committeeCallRepository).save(call);
        verify(subscriptionService).getAllSubscribersByTopic(SubscripeTo.COMMITTEE, committee.getId());
    }
}
