package com.acm.acmwebsite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acm.acmwebsite.User_Authentication.entity.EmailConfirmationRequest;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.EmailAlreadyConfirmedException;
import com.acm.acmwebsite.User_Authentication.exception.RateLimitException;
import com.acm.acmwebsite.User_Authentication.repository.EmailConfirmationRequestRepository;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.impl.EmailConfirmationServiceImpl;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailConfirmationServiceImpl Unit Tests")
class EmailConfirmationServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private EmailConfirmationRequestRepository rateLimitRepository;
  @Mock private JwtUtil jwtUtil;
  @Mock private EmailService emailService;

  @InjectMocks private EmailConfirmationServiceImpl emailConfirmationService;

  private User buildUser(String email, String name, Boolean emailConfirmed) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .name(name)
        .emailConfirmed(emailConfirmed)
        .build();
  }

  @Nested
  @DisplayName("sendConfirmationEmail")
  class SendConfirmationEmailTests {

    @Test
    @DisplayName("should send confirmation email and record rate limit request when user is valid and unconfirmed")
    void shouldSendConfirmationEmailAndSaveRateLimitWhenUserValidAndUnconfirmed() {
      String email = "john.doe@acm.org";
      String userName = "John Doe";
      User user = buildUser(email, userName, false);

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(rateLimitRepository.countByEmailAndRequestedAtAfter(eq(email), any(LocalDateTime.class)))
          .thenReturn(0L);
      when(jwtUtil.generateEmailConfirmationToken(email)).thenReturn("raw-jwt-token");
      when(jwtUtil.encodeTokenForUrl("raw-jwt-token")).thenReturn("encoded-url-token");

      LocalDateTime beforeCall = LocalDateTime.now();
      emailConfirmationService.sendConfirmationEmail(email);
      LocalDateTime afterCall = LocalDateTime.now();

      verify(jwtUtil).generateEmailConfirmationToken(email);
      verify(jwtUtil).encodeTokenForUrl("raw-jwt-token");

      ArgumentCaptor<EmailConfirmationRequest> captor =
          ArgumentCaptor.forClass(EmailConfirmationRequest.class);
      verify(rateLimitRepository).save(captor.capture());
      EmailConfirmationRequest savedRequest = captor.getValue();
      assertThat(savedRequest.getEmail()).isEqualTo(email);
      assertThat(savedRequest.getRequestedAt())
          .isAfterOrEqualTo(beforeCall.minusSeconds(1))
          .isBeforeOrEqualTo(afterCall.plusSeconds(1));

      verify(emailService).sendEmailConfirmationEmail(email, "encoded-url-token", userName);
    }

    @Test
    @DisplayName("should return silently when user does not exist in repository")
    void shouldReturnSilentlyWhenUserNotFound() {
      String email = "missing@acm.org";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      emailConfirmationService.sendConfirmationEmail(email);

      verify(rateLimitRepository, never()).countByEmailAndRequestedAtAfter(any(), any());
      verify(jwtUtil, never()).generateEmailConfirmationToken(any());
      verify(jwtUtil, never()).encodeTokenForUrl(any());
      verify(rateLimitRepository, never()).save(any());
      verify(emailService, never()).sendEmailConfirmationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("should throw EmailAlreadyConfirmedException when user email is already confirmed")
    void shouldThrowEmailAlreadyConfirmedExceptionWhenUserAlreadyConfirmed() {
      String email = "confirmed@acm.org";
      User user = buildUser(email, "Confirmed User", true);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

      assertThatThrownBy(() -> emailConfirmationService.sendConfirmationEmail(email))
          .isInstanceOf(EmailAlreadyConfirmedException.class)
          .hasMessage("Email already confirmed.");

      verify(rateLimitRepository, never()).countByEmailAndRequestedAtAfter(any(), any());
      verify(jwtUtil, never()).generateEmailConfirmationToken(any());
      verify(emailService, never()).sendEmailConfirmationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("should throw RateLimitException when confirmation request count is one or more within one minute")
    void shouldThrowRateLimitExceptionWhenRequestedWithinOneMinute() {
      String email = "frequent@acm.org";
      User user = buildUser(email, "Frequent User", false);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(rateLimitRepository.countByEmailAndRequestedAtAfter(eq(email), any(LocalDateTime.class)))
          .thenReturn(1L);

      assertThatThrownBy(() -> emailConfirmationService.sendConfirmationEmail(email))
          .isInstanceOf(RateLimitException.class)
          .hasMessage("Confirmation email already sent. Please wait before requesting again.");

      verify(jwtUtil, never()).generateEmailConfirmationToken(any());
      verify(jwtUtil, never()).encodeTokenForUrl(any());
      verify(rateLimitRepository, never()).save(any());
      verify(emailService, never()).sendEmailConfirmationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("should treat null emailConfirmed as unconfirmed and proceed to send email")
    void shouldTreatNullEmailConfirmedAsUnconfirmed() {
      String email = "nullconfirmed@acm.org";
      User user = buildUser(email, "Null Confirmed User", null);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(rateLimitRepository.countByEmailAndRequestedAtAfter(eq(email), any(LocalDateTime.class)))
          .thenReturn(0L);
      when(jwtUtil.generateEmailConfirmationToken(email)).thenReturn("raw-token");
      when(jwtUtil.encodeTokenForUrl("raw-token")).thenReturn("encoded-token");

      emailConfirmationService.sendConfirmationEmail(email);

      verify(emailService).sendEmailConfirmationEmail(email, "encoded-token", "Null Confirmed User");
      verify(rateLimitRepository).save(any(EmailConfirmationRequest.class));
    }
  }

  @Nested
  @DisplayName("confirmEmail")
  class ConfirmEmailTests {

    @Test
    @DisplayName("should confirm user email, persist changes, and send welcome email when token is valid")
    void shouldConfirmEmailAndSendWelcomeEmailWhenTokenValid() {
      String encodedToken = "valid-encoded-token";
      String decodedToken = "valid-raw-jwt";
      String email = "newuser@acm.org";
      User user = buildUser(email, "New User", false);

      when(jwtUtil.decodeTokenFromUrl(encodedToken)).thenReturn(decodedToken);
      when(jwtUtil.validateEmailConfirmationToken(decodedToken)).thenReturn(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

      emailConfirmationService.confirmEmail(encodedToken);

      assertThat(user.getEmailConfirmed()).isTrue();
      verify(userRepository).save(user);
      verify(emailService).sendWelcomeEmail(email, "ACM Member");
    }

    @Test
    @DisplayName("should still successfully confirm email when sending welcome email throws an exception")
    void shouldStillConfirmEmailWhenWelcomeEmailThrowsException() {
      String encodedToken = "encoded-token";
      String decodedToken = "raw-jwt";
      String email = "user@acm.org";
      User user = buildUser(email, "User", false);

      when(jwtUtil.decodeTokenFromUrl(encodedToken)).thenReturn(decodedToken);
      when(jwtUtil.validateEmailConfirmationToken(decodedToken)).thenReturn(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      doThrow(new RuntimeException("Mail server connection failed"))
          .when(emailService)
          .sendWelcomeEmail(email, "ACM Member");

      assertThatCode(() -> emailConfirmationService.confirmEmail(encodedToken))
          .doesNotThrowAnyException();

      assertThat(user.getEmailConfirmed()).isTrue();
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("should return silently when user from valid token is not found in repository")
    void shouldReturnSilentlyWhenUserNotFound() {
      String encodedToken = "encoded-token";
      String decodedToken = "raw-jwt";
      String email = "unknown@acm.org";

      when(jwtUtil.decodeTokenFromUrl(encodedToken)).thenReturn(decodedToken);
      when(jwtUtil.validateEmailConfirmationToken(decodedToken)).thenReturn(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      emailConfirmationService.confirmEmail(encodedToken);

      verify(userRepository, never()).save(any());
      verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    @DisplayName("should throw EmailAlreadyConfirmedException when user is already confirmed")
    void shouldThrowEmailAlreadyConfirmedExceptionWhenUserAlreadyConfirmed() {
      String encodedToken = "encoded-token";
      String decodedToken = "raw-jwt";
      String email = "alreadyconfirmed@acm.org";
      User user = buildUser(email, "Already Confirmed User", true);

      when(jwtUtil.decodeTokenFromUrl(encodedToken)).thenReturn(decodedToken);
      when(jwtUtil.validateEmailConfirmationToken(decodedToken)).thenReturn(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

      assertThatThrownBy(() -> emailConfirmationService.confirmEmail(encodedToken))
          .isInstanceOf(EmailAlreadyConfirmedException.class)
          .hasMessage("Email already confirmed.");

      verify(userRepository, never()).save(any());
      verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    @DisplayName("should propagate exception when decoding token fails")
    void shouldPropagateExceptionWhenTokenDecodingFails() {
      String invalidToken = "invalid-token";
      when(jwtUtil.decodeTokenFromUrl(invalidToken))
          .thenThrow(new IllegalArgumentException("Invalid or tampered confirmation token"));

      assertThatThrownBy(() -> emailConfirmationService.confirmEmail(invalidToken))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid or tampered confirmation token");

      verify(jwtUtil, never()).validateEmailConfirmationToken(any());
      verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("should propagate exception when validating token fails")
    void shouldPropagateExceptionWhenTokenValidationFails() {
      String encodedToken = "encoded-token";
      String decodedToken = "raw-jwt";

      when(jwtUtil.decodeTokenFromUrl(encodedToken)).thenReturn(decodedToken);
      when(jwtUtil.validateEmailConfirmationToken(decodedToken))
          .thenThrow(new RuntimeException("Invalid email confirmation token"));

      assertThatThrownBy(() -> emailConfirmationService.confirmEmail(encodedToken))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Invalid email confirmation token");

      verify(userRepository, never()).findByEmail(any());
    }
  }
}
