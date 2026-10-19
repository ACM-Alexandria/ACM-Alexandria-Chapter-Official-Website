package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.feature.entity.ClubRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.service.ClubRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubRegistrationRepository clubRegistrationRepository;
    @Mock
    private ClubFormQuestionRepository clubFormQuestionRepository;
    @Mock
    private EmailService coreEmailService;

    @InjectMocks
    private ClubRegistrationService clubRegistrationService;

    @Nested
    @DisplayName("getQuestions Tests")
    class GetQuestionsTests {

        @Test
        @DisplayName("getQuestions should return mapped DTOs when club exists")
        void getClubQuestions_shouldReturnQuestions() {
            when(clubRepository.existsById(1L)).thenReturn(true);
            ClubFormQuestion question = ClubFormQuestion.builder()
                    .id(10L)
                    .questionText("Why do you want to join?")
                    .questionType(QuestionType.TEXT)
                    .isRequired(true)
                    .options(List.of("A", "B"))
                    .build();
            when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of(question));

            List<FormQuestionResponseDto> result = clubRegistrationService.getQuestions(1L);

            assertFalse(result.isEmpty());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("Why do you want to join?", result.getFirst().getQuestionText());
            assertEquals("TEXT", result.getFirst().getQuestionType());
            assertTrue(result.getFirst().getIsRequired());
            assertEquals(List.of("A", "B"), result.getFirst().getOptions());
        }

        @Test
        @DisplayName("getQuestions club not found should throw ResourceNotFoundException")
        void getClubQuestions_clubNotFound_shouldThrow() {
            when(clubRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clubRegistrationService.getQuestions(1L));
            assertEquals("Club not found with id 1", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("registerUser Tests")
    class RegisterUserTests {

        private User createCompleteUser(UUID userId, Role role) {
            return User.builder()
                    .id(userId)
                    .email("test@acm.org")
                    .name("ACM Member")
                    .phoneNumber("123456")
                    .isAlexEngStudent(true)
                    .role(role)
                    .build();
        }

        private Club createClub(Long id, boolean open, boolean external) {
            Club club = new Club();
            club.setId(id);
            club.setName("ACM AI Club");
            club.setRegistrationOpen(open);
            club.setIsExternal(external);
            return club;
        }

        @Test
        @DisplayName("registerUser successful registration with external club")
        void registerUserForClub_successful() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            Club club = createClub(1L, true, true);

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
            verify(eventPublisher).publishEvent(any(SheetSyncEvent.class));
        }

        @Test
        @DisplayName("registerUser email failure does not block registration")
        void registerUser_emailFailure_doesNotThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            Club club = createClub(1L, true, true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            request.setAnswers(Map.of());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);
            when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of());
            doThrow(new RuntimeException("Mail server down")).when(coreEmailService)
                    .sendRegistrationConfirmationEmail(any(), any(), any());

            assertDoesNotThrow(() -> clubRegistrationService.registerUser(userId, 1L, request));
            verify(clubRegistrationRepository).save(any(ClubRegistration.class));
        }

        @Test
        @DisplayName("registerUser user not found should throw ResourceNotFoundException")
        void registerUser_userNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clubRegistrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser club not found should throw ResourceNotFoundException")
        void registerUser_clubNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clubRegistrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Club not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser when registration closed should throw IllegalArgumentException")
        void registerUser_registrationClosed_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            Club club = createClub(1L, false, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubRegistrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Registration is closed for this club.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser internal club with USER role should throw IllegalArgumentException")
        void registerUser_internalClub_userRole_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            Club club = createClub(1L, true, false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubRegistrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Registration for internal clubs is restricted to ACM members.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser internal club with null role should throw IllegalArgumentException")
        void registerUser_internalClub_nullRole_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, null);
            Club club = createClub(1L, true, false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubRegistrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Registration for internal clubs is restricted to ACM members.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser internal club with ACM_MEMBER role should succeed")
        void registerUser_internalClub_acmMember_shouldSucceed() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.ACM_MEMBER);
            Club club = createClub(1L, true, false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);
            when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of());

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setAnswers(Map.of());

            assertDoesNotThrow(() -> clubRegistrationService.registerUser(userId, 1L, request));
            verify(clubRegistrationRepository).save(any());
        }

        @Test
        @DisplayName("registerUser with incomplete profile should throw ProfileIncompleteException")
        void registerUserForClub_incompleteProfile_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User incompleteUser = User.builder()
                    .id(userId)
                    .name("")
                    .build();

            Club club = createClub(1L, true, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(incompleteUser));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(ProfileIncompleteException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser duplicate registration should throw DuplicateRegistrationException")
        void registerUserForClub_duplicateRegistration_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            Club club = createClub(1L, true, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(DuplicateRegistrationException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser missing required answer should throw MissingRequiredAnswerException")
        void registerUserForClub_missingRequiredAnswer_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId, Role.USER);
            ClubFormQuestion requiredQuestion = ClubFormQuestion.builder()
                    .id(200L)
                    .questionText("Necessary answer")
                    .isRequired(true)
                    .build();

            Club club = createClub(1L, true, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);
            when(clubFormQuestionRepository.findByClubId(1L)).thenReturn(List.of(requiredQuestion));

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setAnswers(new HashMap<>());

            assertThrows(MissingRequiredAnswerException.class, () -> clubRegistrationService.registerUser(userId, 1L, request));
        }
    }

    @Nested
    @DisplayName("isUserRegistered Tests")
    class IsUserRegisteredTests {

        @Test
        @DisplayName("isUserRegistered should return true when registered")
        void isUserRegistered_true() {
            UUID userId = UUID.randomUUID();
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(true);

            assertTrue(clubRegistrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered should return false when not registered")
        void isUserRegistered_false() {
            UUID userId = UUID.randomUUID();
            when(clubRegistrationRepository.existsByUserIdAndClubId(userId, 1L)).thenReturn(false);

            assertFalse(clubRegistrationService.isUserRegistered(userId, 1L));
        }
    }

    @Nested
    @DisplayName("cancelRegistration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("cancelRegistration club not found should throw ResourceNotFoundException")
        void cancelRegistration_clubNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(clubRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clubRegistrationService.cancelRegistration(userId, 1L));
            assertEquals("Club not found", ex.getMessage());
            verify(clubRegistrationRepository, never()).deleteByUserIdAndClubId(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration registration not found should throw ResourceNotFoundException")
        void cancelRegistration_registrationNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(clubRepository.existsById(1L)).thenReturn(true);
            when(clubRegistrationRepository.deleteByUserIdAndClubId(userId, 1L)).thenReturn(0L);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clubRegistrationService.cancelRegistration(userId, 1L));
            assertEquals("Club registration not found", ex.getMessage());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration success should delete and publish SheetSyncEvent")
        void cancelRegistration_success() {
            UUID userId = UUID.randomUUID();
            when(clubRepository.existsById(1L)).thenReturn(true);
            when(clubRegistrationRepository.deleteByUserIdAndClubId(userId, 1L)).thenReturn(1L);

            clubRegistrationService.cancelRegistration(userId, 1L);

            verify(clubRegistrationRepository).deleteByUserIdAndClubId(userId, 1L);
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.CLUB, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }
    }
}
