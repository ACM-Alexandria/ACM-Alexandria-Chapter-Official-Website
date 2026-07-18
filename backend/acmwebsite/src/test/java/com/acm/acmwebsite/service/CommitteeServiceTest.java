package com.acm.acmwebsite.service;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommitteeServiceTest {

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

        verify(emailService, times(1)).sendCommitteeCallEmail(user.getEmail(), testMessage.getSubject(), testMessage.getBody());

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

        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any());
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

        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any());

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
        verify(emailService, never()).sendCommitteeCallEmail(any(String.class), any(), any());
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
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(null, "John", "img", "President", 1, "link");
        CommitteeBoard boardEntity = new CommitteeBoard(null, "John", "img", "President", 1, "link");
        CommitteeBoard savedEntity = new CommitteeBoard(1L, "John", "img", "President", 1, "link");
        CommitteeBoardMemberDto savedDto = new CommitteeBoardMemberDto(1L, "John", "img", "President", 1, "link");

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeMapper.toBoardEntity(dto)).thenReturn(boardEntity);
        when(committeeBoardRepository.save(boardEntity)).thenReturn(savedEntity);
        when(committeeMapper.toBoardDto(savedEntity)).thenReturn(savedDto);

        CommitteeBoardMemberDto result = committeService.addCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    @DisplayName("updateCommitteeBoardMember updates existing entity fields")
    void updateCommitteeBoardMemberSuccessfully() {
        CommitteeBoard boardEntity = new CommitteeBoard(1L, "Old Name", "img", "President", 1, "link");
        CommitteeBoardMemberDto dto = new CommitteeBoardMemberDto(1L, "New Name", "new_img", "Vice", 2, "new_link");
        CommitteeBoard savedEntity = new CommitteeBoard(1L, "New Name", "new_img", "Vice", 2, "new_link");
        CommitteeBoardMemberDto savedDto = new CommitteeBoardMemberDto(1L, "New Name", "new_img", "Vice", 2, "new_link");

        when(committeeBoardRepository.findById(1L)).thenReturn(Optional.of(boardEntity));
        when(committeeBoardRepository.save(boardEntity)).thenReturn(savedEntity);
        when(committeeMapper.toBoardDto(savedEntity)).thenReturn(savedDto);

        CommitteeBoardMemberDto result = committeService.updateCommitteeBoardMember(1L, dto);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
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

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(commiteeRepository.save(any(Committee.class))).thenReturn(committee);
        when(subscriptionService.getAllSubscribersByTopic(any(), any())).thenReturn(List.of());

        committeService.openCommitteeCall(1L);

        assertTrue(committee.isOpen());
        verify(committeeCallRepository, times(1)).save(any(CommitteeCall.class));
    }

    @Test
    @DisplayName("openCommitteeCall throws exception when already open")
    void openCommitteeCallThrowsWhenAlreadyOpen() {
        Committee committee = createDummyCommittee();
        committee.setOpen(true);

        when(commiteeRepository.findById(1L)).thenReturn(Optional.of(committee));

        assertThrows(IllegalStateException.class, () -> committeService.openCommitteeCall(1L));
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

}