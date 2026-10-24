package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.User_Authentication.controller.AuthController;
import com.acm.acmwebsite.User_Authentication.dto.ConfirmEmailRequest;
import com.acm.acmwebsite.User_Authentication.dto.ForgotPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.GoogleLoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginResponse;
import com.acm.acmwebsite.User_Authentication.dto.LogoutRequest;
import com.acm.acmwebsite.User_Authentication.dto.RefreshTokenRequest;
import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.ResendConfirmationRequest;
import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.EmailAlreadyConfirmedException;
import com.acm.acmwebsite.User_Authentication.exception.ExpiredRefreshTokenException;
import com.acm.acmwebsite.User_Authentication.exception.InvalidRefreshTokenException;
import com.acm.acmwebsite.User_Authentication.exception.RateLimitException;
import com.acm.acmwebsite.User_Authentication.exception.TokenExceptionHandler;
import com.acm.acmwebsite.User_Authentication.exception.UserExceptionHandler;
import com.acm.acmwebsite.User_Authentication.service.EmailConfirmationService;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import com.acm.acmwebsite.User_Authentication.service.TokenService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserExceptionHandler.class, TokenExceptionHandler.class})
@DisplayName("AuthController WebMvc Tests")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;
  @MockitoBean private TokenService tokenService;
  @MockitoBean private RegisterService registerService;
  @MockitoBean private EmailConfirmationService emailConfirmationService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private RateLimitInterceptor rateLimitInterceptor;

  @BeforeEach
  void setUp() throws Exception {
    when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Nested
  @DisplayName("GET /api/v1/auth/currentUser")
  class GetCurrentUserTests {

    @Test
    @DisplayName("should return 401 Unauthorized when authentication is null")
    void shouldReturn401WhenAuthenticationIsNull() throws Exception {
      mockMvc.perform(get("/api/v1/auth/currentUser"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(userService, never()).getUserAuthDetails(any());
    }

    @Test
    @DisplayName("should return 401 Unauthorized when authentication has null name")
    void shouldReturn401WhenAuthenticationNameIsNull() throws Exception {
      org.springframework.security.core.Authentication authWithNullName =
          org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
      when(authWithNullName.getName()).thenReturn(null);

      mockMvc.perform(get("/api/v1/auth/currentUser").principal(authWithNullName))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(userService, never()).getUserAuthDetails(any());
    }

    @Test
    @DisplayName("should return 200 OK with user details when authentication is valid")
    void shouldReturn200WithUserDetailsWhenAuthenticationIsValid() throws Exception {
      String email = "john.doe@acm.org";
      UUID userId = UUID.randomUUID();
      Map<String, Object> details = Map.of("id", userId.toString(), "email", email, "role", "USER");

      when(userService.getUserAuthDetails(email)).thenReturn(details);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

      mockMvc.perform(get("/api/v1/auth/currentUser").principal(auth))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", equalTo(email)))
          .andExpect(jsonPath("$.role", equalTo("USER")))
          .andExpect(jsonPath("$.id", equalTo(userId.toString())));

      verify(userService).getUserAuthDetails(email);
    }

    @Test
    @DisplayName("should return 401 Unauthorized when service throws exception")
    void shouldReturn401WhenUserServiceThrowsException() throws Exception {
      String email = "invalid@acm.org";
      when(userService.getUserAuthDetails(email)).thenThrow(new RuntimeException("User not found"));

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

      mockMvc.perform(get("/api/v1/auth/currentUser").principal(auth))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("User session invalid")));

      verify(userService).getUserAuthDetails(email);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/register")
  class RegisterUserTests {

    @Test
    @DisplayName("should return 201 Created when registration is successful")
    void shouldReturn201WhenRegistrationSuccessful() throws Exception {
      RegisterDTO request =
          RegisterDTO.builder()
              .email("newuser@acm.org")
              .password("StrongPass123")
              .passwordConfirmation("StrongPass123")
              .build();

      UUID userId = UUID.randomUUID();
      SuccessRegisterResponse response = new SuccessRegisterResponse(userId, "newuser@acm.org");

      when(registerService.createUser(any(RegisterDTO.class))).thenReturn(response);

      mockMvc.perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo("newuser@acm.org")));

      verify(registerService).createUser(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("should return 409 Conflict when email is already taken")
    void shouldReturn409WhenDuplicateEmail() throws Exception {
      RegisterDTO request =
          RegisterDTO.builder()
              .email("existing@acm.org")
              .password("StrongPass123")
              .passwordConfirmation("StrongPass123")
              .build();

      when(registerService.createUser(any(RegisterDTO.class)))
          .thenThrow(new DuplicateEmailException("Email is already taken"));

      mockMvc.perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error", equalTo("Email is already taken")));

      verify(registerService).createUser(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("should return 400 Bad Request when request body has validation errors")
    void shouldReturn400WhenValidationFails() throws Exception {
      RegisterDTO invalidRequest =
          RegisterDTO.builder()
              .email("not-an-email")
              .password("weak")
              .passwordConfirmation("")
              .build();

      mockMvc.perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("email: Invalid Email Format"));

      verify(registerService, never()).createUser(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/confirm-email")
  class ConfirmEmailTests {

    @Test
    @DisplayName("should return 200 OK when confirmation succeeds")
    void shouldReturn200WhenConfirmationSucceeds() throws Exception {
      ConfirmEmailRequest request = new ConfirmEmailRequest();
      request.setToken("valid-token");

      doNothing().when(emailConfirmationService).confirmEmail("valid-token");

      mockMvc.perform(
              post("/api/v1/auth/confirm-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message", equalTo("Email confirmed successfully.")));

      verify(emailConfirmationService).confirmEmail("valid-token");
    }

    @Test
    @DisplayName("should return 429 Too Many Requests when rate limit is exceeded")
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
      ConfirmEmailRequest request = new ConfirmEmailRequest();
      request.setToken("rate-limited-token");

      doThrow(new RateLimitException("Rate limit reached"))
          .when(emailConfirmationService)
          .confirmEmail("rate-limited-token");

      mockMvc.perform(
              post("/api/v1/auth/confirm-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isTooManyRequests())
          .andExpect(jsonPath("$.error", equalTo("Rate limit reached")));

      verify(emailConfirmationService).confirmEmail("rate-limited-token");
    }

    @Test
    @DisplayName("should return 409 Conflict when email is already confirmed")
    void shouldReturn409WhenEmailAlreadyConfirmed() throws Exception {
      ConfirmEmailRequest request = new ConfirmEmailRequest();
      request.setToken("already-confirmed-token");

      doThrow(new EmailAlreadyConfirmedException("Email is already confirmed"))
          .when(emailConfirmationService)
          .confirmEmail("already-confirmed-token");

      mockMvc.perform(
              post("/api/v1/auth/confirm-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error", equalTo("Email is already confirmed")));

      verify(emailConfirmationService).confirmEmail("already-confirmed-token");
    }

    @Test
    @DisplayName("should return 400 Bad Request when confirmation fails with generic exception")
    void shouldReturn400WhenConfirmationThrowsGenericException() throws Exception {
      ConfirmEmailRequest request = new ConfirmEmailRequest();
      request.setToken("invalid-or-expired-token");

      doThrow(new RuntimeException("Token expired or invalid"))
          .when(emailConfirmationService)
          .confirmEmail("invalid-or-expired-token");

      mockMvc.perform(
              post("/api/v1/auth/confirm-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Invalid or expired confirmation Link.")));

      verify(emailConfirmationService).confirmEmail("invalid-or-expired-token");
    }

    @Test
    @DisplayName("should return 400 Bad Request when token is blank")
    void shouldReturn400WhenTokenIsBlank() throws Exception {
      ConfirmEmailRequest request = new ConfirmEmailRequest();
      request.setToken("");

      mockMvc.perform(
              post("/api/v1/auth/confirm-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(emailConfirmationService, never()).confirmEmail(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/resend-confirmation-email")
  class ResendConfirmationTests {

    @Test
    @DisplayName("should return 200 OK when confirmation email is resent successfully")
    void shouldReturn200WhenResendSucceeds() throws Exception {
      ResendConfirmationRequest request = new ResendConfirmationRequest();
      request.setEmail("user@acm.org");

      doNothing().when(emailConfirmationService).sendConfirmationEmail("user@acm.org");

      mockMvc.perform(
              post("/api/v1/auth/resend-confirmation-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath(
                  "$.message",
                  equalTo(
                      "If an unconfirmed account with this email exists, a confirmation link has been sent.")));

      verify(emailConfirmationService).sendConfirmationEmail("user@acm.org");
    }

    @Test
    @DisplayName("should return 429 Too Many Requests when resending is rate limited")
    void shouldReturn429WhenResendRateLimited() throws Exception {
      ResendConfirmationRequest request = new ResendConfirmationRequest();
      request.setEmail("user@acm.org");

      doThrow(new RateLimitException("Please wait before requesting again"))
          .when(emailConfirmationService)
          .sendConfirmationEmail("user@acm.org");

      mockMvc.perform(
              post("/api/v1/auth/resend-confirmation-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isTooManyRequests())
          .andExpect(jsonPath("$.error", equalTo("Please wait before requesting again")));

      verify(emailConfirmationService).sendConfirmationEmail("user@acm.org");
    }

    @Test
    @DisplayName("should return 409 Conflict when email is already confirmed")
    void shouldReturn409WhenEmailAlreadyConfirmed() throws Exception {
      ResendConfirmationRequest request = new ResendConfirmationRequest();
      request.setEmail("user@acm.org");

      doThrow(new EmailAlreadyConfirmedException("Account is already verified"))
          .when(emailConfirmationService)
          .sendConfirmationEmail("user@acm.org");

      mockMvc.perform(
              post("/api/v1/auth/resend-confirmation-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error", equalTo("Account is already verified")));

      verify(emailConfirmationService).sendConfirmationEmail("user@acm.org");
    }

    @Test
    @DisplayName("should return 500 Internal Server Error when mail sending fails unexpectedly")
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
      ResendConfirmationRequest request = new ResendConfirmationRequest();
      request.setEmail("user@acm.org");

      doThrow(new RuntimeException("Mail server down"))
          .when(emailConfirmationService)
          .sendConfirmationEmail("user@acm.org");

      mockMvc.perform(
              post("/api/v1/auth/resend-confirmation-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isInternalServerError())
          .andExpect(
              jsonPath(
                  "$.error",
                  equalTo(
                      "An error occurred while sending the confirmation email. Please try again later.")));

      verify(emailConfirmationService).sendConfirmationEmail("user@acm.org");
    }

    @Test
    @DisplayName("should return 400 Bad Request when email format is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
      ResendConfirmationRequest request = new ResendConfirmationRequest();
      request.setEmail("not-an-email");

      mockMvc.perform(
              post("/api/v1/auth/resend-confirmation-email")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(emailConfirmationService, never()).sendConfirmationEmail(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/login")
  class LoginUserTests {

    @Test
    @DisplayName("should return 200 OK with LoginResponse when credentials are valid")
    void shouldReturn200WhenCredentialsAreValid() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setEmail("member@acm.org");
      request.setPassword("SecretPass123");

      UUID userId = UUID.randomUUID();
      LoginResponse response =
          LoginResponse.builder()
              .id(userId)
              .email("member@acm.org")
              .accessToken("valid-access-token")
              .refreshToken("valid-refresh-token")
              .role("USER")
              .build();

      when(userService.login(any(LoginRequest.class))).thenReturn(response);

      mockMvc.perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo("member@acm.org")))
          .andExpect(jsonPath("$.accessToken", equalTo("valid-access-token")))
          .andExpect(jsonPath("$.refreshToken", equalTo("valid-refresh-token")))
          .andExpect(jsonPath("$.role", equalTo("USER")));

      verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("should return 403 Forbidden when email is not confirmed")
    void shouldReturn403WhenEmailNotConfirmed() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setEmail("unconfirmed@acm.org");
      request.setPassword("SecretPass123");

      when(userService.login(any(LoginRequest.class)))
          .thenThrow(new IllegalStateException("Please confirm your email before logging in"));

      mockMvc.perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(content().string("Please confirm your email before logging in"));

      verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("should return 401 Unauthorized when credentials are incorrect")
    void shouldReturn401WhenCredentialsIncorrect() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setEmail("member@acm.org");
      request.setPassword("WrongPassword");

      when(userService.login(any(LoginRequest.class)))
          .thenThrow(new IllegalArgumentException("Incorrect email or password"));

      mockMvc.perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Incorrect email or password")));

      verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("should return 400 Bad Request when login request validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setEmail("");
      request.setPassword("");

      mockMvc.perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).login(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/refresh")
  class RefreshAccessTokenTests {

    @Test
    @DisplayName("should return 200 OK with new tokens when refresh token is valid")
    void shouldReturn200WhenRefreshTokenIsValid() throws Exception {
      RefreshTokenRequest request = new RefreshTokenRequest();
      request.setRefreshToken("valid-refresh-token");

      User user = User.builder().id(UUID.randomUUID()).email("user@acm.org").build();

      doNothing().when(tokenService).validateRefreshToken("valid-refresh-token");
      when(tokenService.getUserFromRefreshToken("valid-refresh-token")).thenReturn(user);
      doNothing().when(tokenService).revokeRefreshToken("valid-refresh-token");
      when(tokenService.createAccessToken(user)).thenReturn("new-access-token");
      when(tokenService.createRefreshToken(user)).thenReturn("new-refresh-token");

      mockMvc.perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.access_token", equalTo("new-access-token")))
          .andExpect(jsonPath("$.refresh_token", equalTo("new-refresh-token")));

      verify(tokenService).validateRefreshToken("valid-refresh-token");
      verify(tokenService).getUserFromRefreshToken("valid-refresh-token");
      verify(tokenService).revokeRefreshToken("valid-refresh-token");
      verify(tokenService).createAccessToken(user);
      verify(tokenService).createRefreshToken(user);
    }

    @Test
    @DisplayName("should return 401 Unauthorized when refresh token is invalid")
    void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
      RefreshTokenRequest request = new RefreshTokenRequest();
      request.setRefreshToken("invalid-token");

      doThrow(new InvalidRefreshTokenException("Invalid refresh token"))
          .when(tokenService)
          .validateRefreshToken("invalid-token");

      mockMvc.perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Invalid refresh token")));

      verify(tokenService).validateRefreshToken("invalid-token");
      verify(tokenService, never()).getUserFromRefreshToken(any());
    }

    @Test
    @DisplayName("should return 401 Unauthorized when refresh token is expired")
    void shouldReturn401WhenRefreshTokenIsExpired() throws Exception {
      RefreshTokenRequest request = new RefreshTokenRequest();
      request.setRefreshToken("expired-token");

      doThrow(new ExpiredRefreshTokenException("Refresh token has expired"))
          .when(tokenService)
          .validateRefreshToken("expired-token");

      mockMvc.perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Refresh token has expired")));

      verify(tokenService).validateRefreshToken("expired-token");
      verify(tokenService, never()).getUserFromRefreshToken(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/forgot-password")
  class ForgotPasswordTests {

    @Test
    @DisplayName("should return 200 OK and trigger password reset when email is valid")
    void shouldReturn200WhenEmailIsValid() throws Exception {
      ForgotPasswordDTO request = new ForgotPasswordDTO();
      request.setEmail("user@acm.org");

      doNothing().when(userService).initiatePasswordReset("user@acm.org");

      mockMvc.perform(
              post("/api/v1/auth/forgot-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath(
                  "$.message",
                  equalTo("If an account with this email exists, a password reset link has been sent.")));

      verify(userService).initiatePasswordReset("user@acm.org");
    }

    @Test
    @DisplayName("should return 400 Bad Request when email format is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
      ForgotPasswordDTO request = new ForgotPasswordDTO();
      request.setEmail("invalid-email");

      mockMvc.perform(
              post("/api/v1/auth/forgot-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).initiatePasswordReset(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/reset-password")
  class ResetPasswordTests {

    @Test
    @DisplayName("should return 200 OK when password reset succeeds")
    void shouldReturn200WhenPasswordResetSucceeds() throws Exception {
      ResetPasswordDTO request = new ResetPasswordDTO();
      request.setToken("valid-token");
      request.setNew_password("NewPass123");
      request.setNew_password_confirm("NewPass123");

      doNothing().when(userService).resetPassword(any(ResetPasswordDTO.class));

      mockMvc.perform(
              post("/api/v1/auth/reset-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message", equalTo("Password has been reset successfully.")));

      verify(userService).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    @DisplayName("should return 400 Bad Request when service throws IllegalArgumentException")
    void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
      ResetPasswordDTO request = new ResetPasswordDTO();
      request.setToken("expired-token");
      request.setNew_password("NewPass123");
      request.setNew_password_confirm("NewPass123");

      doThrow(new IllegalArgumentException("Token has expired"))
          .when(userService)
          .resetPassword(any(ResetPasswordDTO.class));

      mockMvc.perform(
              post("/api/v1/auth/reset-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Token has expired")));

      verify(userService).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    @DisplayName("should return 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
      ResetPasswordDTO request = new ResetPasswordDTO();
      request.setToken("");
      request.setNew_password("");
      request.setNew_password_confirm("");

      mockMvc.perform(
              post("/api/v1/auth/reset-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).resetPassword(any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/logout")
  class LogoutTests {

    @Test
    @DisplayName("should return 401 Unauthorized when current user is null")
    void shouldReturn401WhenCurrentUserIsNull() throws Exception {
      LogoutRequest request = new LogoutRequest();
      request.setRefreshToken("some-refresh-token");

      mockMvc.perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(tokenService, never()).revokeRefreshToken(any());
    }

    @Test
    @WithMockUser(username = "user@acm.org")
    @DisplayName("should return 400 Bad Request when refresh token is null")
    void shouldReturn400WhenRefreshTokenIsNull() throws Exception {
      LogoutRequest request = new LogoutRequest();
      request.setRefreshToken(null);

      mockMvc.perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("refresh_token is required")));

      verify(tokenService, never()).revokeRefreshToken(any());
    }

    @Test
    @WithMockUser(username = "user@acm.org")
    @DisplayName("should return 400 Bad Request when refresh token is empty")
    void shouldReturn400WhenRefreshTokenIsEmpty() throws Exception {
      LogoutRequest request = new LogoutRequest();
      request.setRefreshToken("");

      mockMvc.perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("refresh_token is required")));

      verify(tokenService, never()).revokeRefreshToken(any());
    }

    @Test
    @WithMockUser(username = "user@acm.org")
    @DisplayName("should return 200 OK when logout is successful")
    void shouldReturn200WhenLogoutIsSuccessful() throws Exception {
      LogoutRequest request = new LogoutRequest();
      request.setRefreshToken("valid-refresh-token");

      doNothing().when(tokenService).revokeRefreshToken("valid-refresh-token");

      mockMvc.perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("Logged out successfully")));

      verify(tokenService).revokeRefreshToken("valid-refresh-token");
    }

    @Test
    @WithMockUser(username = "user@acm.org")
    @DisplayName("should return 400 Bad Request when revoking token throws exception")
    void shouldReturn400WhenRevokingThrowsException() throws Exception {
      LogoutRequest request = new LogoutRequest();
      request.setRefreshToken("already-revoked-token");

      doThrow(new RuntimeException("Token already revoked"))
          .when(tokenService)
          .revokeRefreshToken("already-revoked-token");

      mockMvc.perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(equalTo("Token already revoked")));

      verify(tokenService).revokeRefreshToken("already-revoked-token");
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/google")
  class LoginWithGoogleTests {

    @Test
    @DisplayName("should return 200 OK with LoginResponse when Google credential is valid")
    void shouldReturn200WhenGoogleCredentialIsValid() throws Exception {
      GoogleLoginRequest request = new GoogleLoginRequest("google-id-token-xyz");

      UUID userId = UUID.randomUUID();
      LoginResponse response =
          LoginResponse.builder()
              .id(userId)
              .email("google.user@acm.org")
              .accessToken("google-access-token")
              .refreshToken("google-refresh-token")
              .role("USER")
              .build();

      when(userService.loginWithGoogle("google-id-token-xyz")).thenReturn(response);

      mockMvc.perform(
              post("/api/v1/auth/google")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo("google.user@acm.org")))
          .andExpect(jsonPath("$.accessToken", equalTo("google-access-token")))
          .andExpect(jsonPath("$.refreshToken", equalTo("google-refresh-token")))
          .andExpect(jsonPath("$.role", equalTo("USER")));

      verify(userService).loginWithGoogle("google-id-token-xyz");
    }

    @Test
    @DisplayName("should return 401 Unauthorized with error message when Google authentication fails")
    void shouldReturn401WhenGoogleAuthFailsWithMessage() throws Exception {
      GoogleLoginRequest request = new GoogleLoginRequest("invalid-id-token");

      when(userService.loginWithGoogle("invalid-id-token"))
          .thenThrow(new RuntimeException("Token signature verification failed"));

      mockMvc.perform(
              post("/api/v1/auth/google")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Token signature verification failed")));

      verify(userService).loginWithGoogle("invalid-id-token");
    }

    @Test
    @DisplayName("should return 401 Unauthorized with fallback message when exception message is null")
    void shouldReturn401WhenGoogleAuthExceptionMessageIsNull() throws Exception {
      GoogleLoginRequest request = new GoogleLoginRequest("invalid-id-token");

      when(userService.loginWithGoogle("invalid-id-token"))
          .thenThrow(new RuntimeException((String) null));

      mockMvc.perform(
              post("/api/v1/auth/google")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Google authentication failed")));

      verify(userService).loginWithGoogle("invalid-id-token");
    }

    @Test
    @DisplayName("should return 400 Bad Request when Google credential is blank")
    void shouldReturn400WhenGoogleCredentialIsBlank() throws Exception {
      GoogleLoginRequest request = new GoogleLoginRequest("");

      mockMvc.perform(
              post("/api/v1/auth/google")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).loginWithGoogle(any());
    }
  }
}
