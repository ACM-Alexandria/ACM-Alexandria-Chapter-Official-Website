package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Committee;
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
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
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

    @Test
    @DisplayName("sendCallMessage throws IllegalArgumentException when message is null")
    void sendCallMessageThrowsWhenMessageIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> committeService.sendCallMessage(SubscripeTo.COMMITTEE, 1L, null));
    }

    @Test
    @DisplayName("sendCallMessage throws IllegalArgumentException when message subject is null")
    void sendCallMessageThrowsWhenSubjectIsNull() {
        Message message = new Message(null, "body content");
        assertThrows(IllegalArgumentException.class,
                () -> committeService.sendCallMessage(SubscripeTo.COMMITTEE, 1L, message));
    }

    @Test
    @DisplayName("sendCallMessage throws IllegalArgumentException when message body is null")
    void sendCallMessageThrowsWhenBodyIsNull() {
        Message message = new Message("Subject", null);
        assertThrows(IllegalArgumentException.class,
                () -> committeService.sendCallMessage(SubscripeTo.COMMITTEE, 1L, message));
    }

    @Test
    @DisplayName("openCommitteeCall throws EntityNotFoundException when committee not found")
    void openCommitteeCallThrowsWhenCommitteeNotFound() {
        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.openCommitteeCall(99L, true));
    }

    @Test
    @DisplayName("openCommitteeCall throws IllegalArgumentException when message subject is null and announcement requested")
    void openCommitteeCallThrowsWhenMessageSubjectIsNull() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(new Message(null, "body"));

        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalArgumentException.class,
                () -> committeService.openCommitteeCall(1L, true));
    }

    @Test
    @DisplayName("openCommitteeCall throws IllegalArgumentException when message body is null and announcement requested")
    void openCommitteeCallThrowsWhenMessageBodyIsNull() {
        Committee committee = createDummyCommittee();
        committee.setOpen(false);
        committee.setCallMessage(new Message("Subject", null));

        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalArgumentException.class,
                () -> committeService.openCommitteeCall(1L, true));
    }

    @Test
    @DisplayName("announceCommitteeCall throws EntityNotFoundException when committee not found")
    void announceCommitteeCallThrowsWhenCommitteeNotFound() {
        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.announceCommitteeCall(99L, false));
    }

    @Test
    @DisplayName("announceCommitteeCall throws IllegalStateException when active call not found")
    void announceCommitteeCallThrowsWhenActiveCallNotFound() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> committeService.announceCommitteeCall(1L, false));
    }

    @Test
    @DisplayName("announceCommitteeCall throws IllegalArgumentException when call message has null subject or body")
    void announceCommitteeCallThrowsWhenMessageInvalid() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);
        committee.setCallMessage(new Message("Subject", null));
        CommitteeCall call = CommitteeCall.builder().id(5L).committee(committee).build();

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(call));

        assertThrows(IllegalArgumentException.class,
                () -> committeService.announceCommitteeCall(1L, false));
    }

    @Test
    @DisplayName("closeCommitteeCall throws EntityNotFoundException when committee not found")
    void closeCommitteeCallThrowsWhenCommitteeNotFound() {
        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.closeCommitteeCall(99L));
    }

    @Test
    @DisplayName("closeCommitteeCall closes call successfully when active call is null")
    void closeCommitteeCallWhenActiveCallIsNull() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.empty());

        committeService.closeCommitteeCall(1L);

        assertFalse(committee.isOpen());
        verify(committeeCallRepository, never()).save(any(CommitteeCall.class));
        verify(commiteeRepository).save(committee);
    }

    @Test
    @DisplayName("saveCommittee throws IllegalArgumentException when committee name is null")
    void saveCommitteeThrowsWhenNameIsNull() {
        Committee committee = new Committee();
        committee.setName(null);

        assertThrows(IllegalArgumentException.class,
                () -> committeService.saveCommittee(committee));
    }

    @Test
    @DisplayName("saveCommittee throws IllegalArgumentException when committee name is empty or blank")
    void saveCommitteeThrowsWhenNameIsBlank() {
        Committee committee = new Committee();
        committee.setName("   ");

        assertThrows(IllegalArgumentException.class,
                () -> committeService.saveCommittee(committee));
    }

    @Test
    @DisplayName("saveCommittee saves and returns committee when name is valid")
    void saveCommitteeSuccessfully() {
        Committee committee = createDummyCommittee();
        when(commiteeRepository.save(committee)).thenReturn(committee);

        Committee result = committeService.saveCommittee(committee);

        assertNotNull(result);
        assertEquals("commiteee", result.getName());
        verify(commiteeRepository).save(committee);
    }

    @Test
    @DisplayName("updateCommittee throws IllegalArgumentException when dto name is empty string")
    void updateCommitteeThrowsWhenNameIsEmpty() {
        CommitteeDto dto = new CommitteeDto();
        dto.setName("   ");

        assertThrows(IllegalArgumentException.class,
                () -> committeService.updateCommittee(1L, dto));
    }

    @Test
    @DisplayName("updateCommittee throws EntityNotFoundException when committee not found")
    void updateCommitteeThrowsWhenNotFound() {
        CommitteeDto dto = new CommitteeDto();
        dto.setName("Updated Committee");

        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.updateCommittee(99L, dto));
    }

    @Test
    @DisplayName("updateCommittee updates committee and returns dto when valid name is provided")
    void updateCommitteeSuccessfullyWithNonNullName() {
        Committee committee = createDummyCommittee();
        CommitteeDto dto = new CommitteeDto();
        dto.setName("Updated Committee");
        dto.setDescription("Updated Description");

        CommitteeDto expectedDto = new CommitteeDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Committee");

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(committee)).thenReturn(committee);
        when(committeeMapper.toDto(committee)).thenReturn(expectedDto);

        CommitteeDto result = committeService.updateCommittee(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Committee", result.getName());
        verify(committeeMapper).updateCommitteeFromDto(dto, committee);
        verify(commiteeRepository).save(committee);
    }

    @Test
    @DisplayName("updateCommittee updates committee and returns dto when dto name is null")
    void updateCommitteeSuccessfullyWithNullName() {
        Committee committee = createDummyCommittee();
        CommitteeDto dto = new CommitteeDto();
        dto.setName(null);
        dto.setDescription("Only description update");

        CommitteeDto expectedDto = new CommitteeDto();
        expectedDto.setId(1L);
        expectedDto.setName("commiteee");

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(committee)).thenReturn(committee);
        when(committeeMapper.toDto(committee)).thenReturn(expectedDto);

        CommitteeDto result = committeService.updateCommittee(1L, dto);

        assertNotNull(result);
        verify(committeeMapper).updateCommitteeFromDto(dto, committee);
        verify(commiteeRepository).save(committee);
    }

    @Test
    @DisplayName("changeCallMessage throws EntityNotFoundException when committee not found")
    void changeCallMessageThrowsWhenCommitteeNotFound() {
        Message message = new Message("New Subject", "New Body");
        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.changeCallMessage(99L, message));
    }

    @Test
    @DisplayName("changeCallMessage saves message and updates committee call message")
    void changeCallMessageSuccessfully() {
        Committee committee = createDummyCommittee();
        Message message = new Message("New Subject", "New Body");
        Message savedMessage = new Message("New Subject", "New Body");

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(messageRepository.save(message)).thenReturn(savedMessage);

        committeService.changeCallMessage(1L, message);

        assertEquals(savedMessage, committee.getCallMessage());
        verify(messageRepository).save(message);
        verify(commiteeRepository).save(committee);
    }

    @Test
    @DisplayName("deleteCommittee delegates deletion to repository")
    void deleteCommitteeSuccessfully() {
        committeService.deleteCommittee(1L);

        verify(commiteeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("addCommitteeBoardMember throws IllegalArgumentException when userId is null")
    void addCommitteeBoardMemberThrowsWhenUserIdIsNull() {
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "President", 1, null);

        assertThrows(IllegalArgumentException.class,
                () -> committeService.addCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("addCommitteeBoardMember throws IllegalArgumentException when role is null")
    void addCommitteeBoardMemberThrowsWhenRoleIsNull() {
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, null, 1, java.util.UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> committeService.addCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("addCommitteeBoardMember throws IllegalArgumentException when role is blank")
    void addCommitteeBoardMemberThrowsWhenRoleIsBlank() {
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "   ", 1, java.util.UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> committeService.addCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("addCommitteeBoardMember throws EntityNotFoundException when committee not found")
    void addCommitteeBoardMemberThrowsWhenCommitteeNotFound() {
        java.util.UUID userId = java.util.UUID.randomUUID();
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "President", 1, userId);

        when(commiteeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.addCommitteeBoardMember(99L, dto));
    }

    @Test
    @DisplayName("addCommitteeBoardMember throws EntityNotFoundException when user not found")
    void addCommitteeBoardMemberThrowsWhenUserNotFound() {
        Committee committee = createDummyCommittee();
        java.util.UUID userId = java.util.UUID.randomUUID();
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "President", 1, userId);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.addCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("updateCommitteeBoardMember throws IllegalArgumentException when role is blank")
    void updateCommitteeBoardMemberThrowsWhenRoleIsBlank() {
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto();
        dto.setRole("   ");

        assertThrows(IllegalArgumentException.class,
                () -> committeService.updateCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("updateCommitteeBoardMember throws EntityNotFoundException when board member not found")
    void updateCommitteeBoardMemberThrowsWhenBoardMemberNotFound() {
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto();
        dto.setRole("Lead");

        when(committeeBoardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.updateCommitteeBoardMember(99L, dto));
    }

    @Test
    @DisplayName("updateCommitteeBoardMember throws EntityNotFoundException when userId provided but user not found")
    void updateCommitteeBoardMemberThrowsWhenUserNotFound() {
        java.util.UUID userId = java.util.UUID.randomUUID();
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "Lead", 1, userId);
        CommitteeBoard boardEntity = new CommitteeBoard();

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> committeService.updateCommitteeBoardMember(1L, dto));
    }

    @Test
    @DisplayName("updateCommitteeBoardMember with null userId updates role and dto fields without user lookup")
    void updateCommitteeBoardMemberWithNullUserId() {
        User existingUser = new User();
        existingUser.setName("Old Name");
        CommitteeBoard boardEntity = new CommitteeBoard(1L, "President", 1, new Committee(), existingUser);

        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto();
        dto.setId(1L);
        dto.setRole("Updated Role");
        dto.setUserId(null);

        CommitteeBoardMemberDto savedDto = new CommitteeBoardMemberDto();
        savedDto.setRole("Updated Role");

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(boardEntity);
        when(committeeMapper.toBoardDto(boardEntity)).thenReturn(savedDto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Role", result.getRole());
        verify(userRepository, never()).findById(any());
        verify(committeeBoardRepository).save(boardEntity);
    }

    @Test
    @DisplayName("updateCommitteeBoardMember updates user profile fields when existingUser is present")
    void updateCommitteeBoardMemberUpdatesUserFields() {
        User existingUser = new User();
        existingUser.setName("Old Name");
        existingUser.setProfileImageUrl("http://old.jpg");
        existingUser.setLinkedinUrl("http://old.linkedin");

        CommitteeBoard boardEntity = new CommitteeBoard(1L, "Lead", 1, new Committee(), existingUser);

        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(
                1L, "Lead", 1, null, "New Name", "http://new.jpg", "http://new.linkedin");

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(boardEntity);
        when(committeeMapper.toBoardDto(boardEntity)).thenReturn(dto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals("New Name", existingUser.getName());
        assertEquals("http://new.jpg", existingUser.getProfileImageUrl());
        assertEquals("http://new.linkedin", existingUser.getLinkedinUrl());
    }

    @Test
    @DisplayName("updateCommitteeBoardMember skips user updates when existingUser is null")
    void updateCommitteeBoardMemberWhenExistingUserIsNull() {
        CommitteeBoard boardEntity = new CommitteeBoard(1L, "Lead", 1, new Committee(), null);
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(
                1L, "Lead", 1, null, "New Name", "http://new.jpg", "http://new.linkedin");

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(boardEntity);
        when(committeeMapper.toBoardDto(boardEntity)).thenReturn(dto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertNull(boardEntity.getUser());
        verify(committeeBoardRepository).save(boardEntity);
    }

    @Test
    @DisplayName("updateCommitteeBoardMember succeeds when dto role is null")
    void updateCommitteeBoardMemberWhenRoleIsNull() {
        CommitteeBoard boardEntity = new CommitteeBoard(1L, "Lead", 1, new Committee(), null);
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto();
        dto.setRole(null);

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(boardEntity);
        when(committeeMapper.toBoardDto(boardEntity)).thenReturn(dto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        verify(committeeBoardRepository).save(boardEntity);
    }
}

