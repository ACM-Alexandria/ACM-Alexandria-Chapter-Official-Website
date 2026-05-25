package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.*;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.service.ClubRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubRegistrationRepository clubRegistrationRepository;
    @Mock
    private ClubFormQuestionRepository clubFormQuestionRepository;
    @Mock
    private com.acm.acmwebsite.core.service.EmailService coreEmailService;

    @InjectMocks
    private ClubRegistrationService clubRegistrationService;

    @Test
    void getClubQuestions_shouldReturnQuestions() {
        when(clubRepository.existsById(1L)).thenReturn(true);
        ClubFormQuestion question = ClubFormQuestion.builder()
                .id(10L)
                .questionText("Why do you want to join?")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .build();
        when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of(question));

        List<FormQuestionResponseDto> result = clubRegistrationService.getQuestions(1L);

        assertFalse(result.isEmpty());
        assertEquals("Why do you want to join?", result.get(0).getQuestionText());
        assertEquals("TEXT", result.get(0).getQuestionType());
    }

    @Test
    void getClubQuestions_clubNotFound_shouldThrow() {
        when(clubRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> clubRegistrationService.getQuestions(1L));
    }

    @Test
    void registerUserForClub_successful() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@acm.org")
                .name("ACM Member")
                .phoneNumber("123456")
                .isAlexEngStudent(true)
                .build();
        Club club = new Club();
        club.setId(1L);
        club.setName("ACM AI Club");

        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setUserId(userId);
        Map<Long, String> answers = new HashMap<>();
        answers.put(100L, "Python");
        request.setAnswers(answers);

        ClubFormQuestion question = ClubFormQuestion.builder()
                .id(100L)
                .questionText("Your fav Language?")
                .isRequired(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);
        when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of(question));

        clubRegistrationService.registerUser(userId, 1L, request);

        verify(clubRegistrationRepository).save(any(ClubRegistration.class));
        verify(coreEmailService).sendRegistrationConfirmationEmail(eq("test@acm.org"), eq("ACM AI Club"), eq("ACM Member"));
    }

    @Test
    void registerUserForClub_incompleteProfile_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User incompleteUser = User.builder()
                .id(userId)
                .name("") // empty name
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(incompleteUser));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(new Club()));

        RegistrationRequestDto request = new RegistrationRequestDto();
        assertThrows(ProfileIncompleteException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
    }

    @Test
    void registerUserForClub_duplicateRegistration_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John")
                .phoneNumber("1111")
                .isAlexEngStudent(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(new Club()));
        when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(true);

        RegistrationRequestDto request = new RegistrationRequestDto();
        assertThrows(DuplicateRegistrationException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
    }

    @Test
    void registerUserForClub_missingRequiredAnswer_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John")
                .phoneNumber("1111")
                .isAlexEngStudent(false)
                .build();

        ClubFormQuestion requiredQuestion = ClubFormQuestion.builder()
                .id(200L)
                .questionText("Necessary answer")
                .isRequired(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(new Club()));
        when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);
        when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of(requiredQuestion));

        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setAnswers(new HashMap<>()); // No answer provided for question 200

        assertThrows(MissingRequiredAnswerException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
    }
}
