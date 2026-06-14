package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.dto.CommitteeCallResponseDto;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.*;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.service.CommitteeRegistrationService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommitteeRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CommitteeRepository committeeRepository;
    @Mock
    private CommitteeCallRepository committeeCallRepository;
    @Mock
    private CommitteeRegistrationRepository committeeRegistrationRepository;
    @Mock
    private CommitteeFormQuestionRepository committeeFormQuestionRepository;
    @Mock
    private com.acm.acmwebsite.core.service.EmailService coreEmailService;
    @Mock
    private GoogleSheetsService googleSheetsService;

    @InjectMocks
    private CommitteeRegistrationService registrationService;

    @Test
    void getQuestions_shouldReturnQuestions() {
        when(committeeRepository.existsById(1L)).thenReturn(true);
        CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                .id(10L)
                .questionText("Stack?")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .build();
        when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of(question));

        List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

        assertFalse(result.isEmpty());
        assertEquals("Stack?", result.get(0).getQuestionText());
    }

    @Test
    void getQuestions_committeeNotFound_shouldThrow() {
        when(committeeRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> registrationService.getQuestions(1L));
    }

    @Test
    void registerUser_successful() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@acm.org")
                .name("ACM Member")
                .phoneNumber("123456")
                .isAlexEngStudent(true)
                .build();

        Committee committee = new Committee();
        committee.setId(1L);
        committee.setName("Tech");
        committee.setOpen(true);

        CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setUserId(userId);
        Map<Long, String> answers = new HashMap<>();
        answers.put(100L, "Java");
        request.setAnswers(answers);

        CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                .id(100L)
                .questionText("Lang?")
                .isRequired(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
        when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(false);
        when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of(question));

        registrationService.registerUser(userId, 1L, request);

        verify(committeeRegistrationRepository).save(any(CommitteeRegistration.class));
        verify(coreEmailService).sendCommitteeRegistrationConfirmationEmail(eq("test@acm.org"), eq("Tech"), eq("ACM Member"));
    }

    @Test
    void registerUser_committeeClosed_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Committee committee = new Committee();
        committee.setId(1L);
        committee.setOpen(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));

        RegistrationRequestDto request = new RegistrationRequestDto();
        assertThrows(IllegalStateException.class, () -> registrationService.registerUser(userId, 1L, request));
    }

    @Test
    void isUserRegistered_shouldReturnStatus() {
        UUID userId = UUID.randomUUID();
        CommitteeCall activeCall = CommitteeCall.builder().id(10L).build();

        when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
        when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(true);

        boolean registered = registrationService.isUserRegistered(userId, 1L);

        assertTrue(registered);
    }

    @Test
    void createQuestion_successful() {
        Committee committee = new Committee();
        committee.setId(1L);

        FormQuestionRequestDto req = new FormQuestionRequestDto("New question", "TEXT", true, List.of());

        when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
        when(committeeFormQuestionRepository.save(any(CommitteeFormQuestion.class))).thenAnswer(i -> i.getArguments()[0]);

        FormQuestionResponseDto result = registrationService.createQuestion(1L, req);

        assertEquals("New question", result.getQuestionText());
        assertTrue(result.getIsRequired());
    }

    @Test
    void getCallsForCommittee_shouldReturnCalls() {
        CommitteeCall call = CommitteeCall.builder()
                .id(10L)
                .openedAt(java.time.LocalDateTime.now())
                .build();

        when(committeeRepository.existsById(1L)).thenReturn(true);
        when(committeeCallRepository.findByCommitteeIdOrderByOpenedAtDesc(1L)).thenReturn(List.of(call));
        when(committeeRegistrationRepository.countByCommitteeCallId(10L)).thenReturn(5L);

        List<CommitteeCallResponseDto> result = registrationService.getCallsForCommittee(1L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getRegistrationsCount());
    }
}
