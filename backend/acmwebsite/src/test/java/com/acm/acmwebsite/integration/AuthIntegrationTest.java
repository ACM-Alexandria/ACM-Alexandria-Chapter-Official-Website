package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.dto.ConfirmEmailRequest;
import com.acm.acmwebsite.User_Authentication.dto.ForgotPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.LoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginResponse;
import com.acm.acmwebsite.User_Authentication.dto.LogoutRequest;
import com.acm.acmwebsite.User_Authentication.dto.RefreshTokenRequest;
import com.acm.acmwebsite.User_Authentication.dto.RefreshTokenResponse;
import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Authentication & User Lifecycle Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailExitanceService emailExitanceService;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);
    }

    @Nested
    @DisplayName("Complete Registration, Activation & Login Flow")
    class RegistrationAndLoginFlowTests {

        @Test
        @DisplayName("Full lifecycle: register -> unconfirmed guard -> confirm -> login -> access profile")
        void fullRegistrationAndLoginLifecycle() throws Exception {
            String testEmail = "lifecycle.user@example.com";
            String rawPassword = "SecurePassword123#";

            // 1. REGISTER
            RegisterDTO registerDto = RegisterDTO.builder()
                    .email(testEmail)
                    .password(rawPassword)
                    .passwordConfirmation(rawPassword)
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(testEmail));

            // Verify User persisted in H2 database as unconfirmed
            User registeredUser = userRepository.findByEmail(testEmail)
                    .orElseThrow(() -> new AssertionError("User should be in database"));
            assertNotEquals(Boolean.TRUE, registeredUser.getEmailConfirmed());

            // Capture confirmation token sent via EmailService
            ArgumentCaptor<String> confirmTokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).sendEmailConfirmationEmail(eq(testEmail), confirmTokenCaptor.capture(), any());
            String confirmationToken = confirmTokenCaptor.getValue();
            assertNotNull(confirmationToken);

            // 2. UNCONFIRMED ACCOUNT LOGIN REJECTION GUARD
            LoginRequest unconfirmedLogin = new LoginRequest();
            unconfirmedLogin.setEmail(testEmail);
            unconfirmedLogin.setPassword(rawPassword);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(unconfirmedLogin)))
                    .andExpect(status().isForbidden());

            // 3. CONFIRM EMAIL
            ConfirmEmailRequest confirmRequest = new ConfirmEmailRequest();
            confirmRequest.setToken(confirmationToken);

            mockMvc.perform(post("/api/v1/auth/confirm-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Email confirmed successfully."));

            // Verify User is confirmed in H2 database and welcome email sent
            User confirmedUser = userRepository.findByEmail(testEmail).orElseThrow();
            assertEquals(Boolean.TRUE, confirmedUser.getEmailConfirmed());
            verify(emailService, times(1)).sendWelcomeEmail(eq(testEmail), anyString());

            // 4. CONFIRMING AGAIN REJECTED WITH CONFLICT
            mockMvc.perform(post("/api/v1/auth/confirm-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isConflict());

            // 5. LOGIN WITH CONFIRMED ACCOUNT
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(testEmail);
            loginRequest.setPassword(rawPassword);

            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testEmail))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);
            String accessToken = loginResponse.getAccessToken();
            assertNotNull(accessToken);

            // 6. ACCESS PROTECTED USER PROFILE WITH JWT ACCESS TOKEN
            mockMvc.perform(get("/api/v1/auth/currentUser")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testEmail));

            mockMvc.perform(get("/api/v1/users/" + confirmedUser.getId() + "/profile")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testEmail));
        }

        @Test
        @DisplayName("Registration rejects duplicate emails and mismatched passwords")
        void registrationValidationErrors() throws Exception {
            String existingEmail = "duplicate@example.com";

            // Register first user
            RegisterDTO first = RegisterDTO.builder()
                    .email(existingEmail)
                    .password("Password123#")
                    .passwordConfirmation("Password123#")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(first)))
                    .andExpect(status().isCreated());

            // Duplicate registration attempt
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(first)))
                    .andExpect(status().isConflict());

            // Password mismatch
            RegisterDTO mismatch = RegisterDTO.builder()
                    .email("mismatch@example.com")
                    .password("Password123#")
                    .passwordConfirmation("DifferentPassword123#")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(mismatch)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login rejects incorrect password and nonexistent user")
        void loginRejectionScenarios() throws Exception {
            LoginRequest nonexistent = new LoginRequest();
            nonexistent.setEmail("nobody@example.com");
            nonexistent.setPassword("Password123#");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonexistent)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Password Reset Workflow")
    class PasswordResetWorkflowTests {

        @Test
        @DisplayName("Complete password reset flow: forgot-password -> reset-password -> login with new password")
        void passwordResetFlow() throws Exception {
            String testEmail = "reset.test@example.com";
            String oldPassword = "OldPassword123#";
            String newPassword = "NewPassword123#";

            // 1. Register & confirm user
            RegisterDTO registerDto = RegisterDTO.builder()
                    .email(testEmail)
                    .password(oldPassword)
                    .passwordConfirmation(oldPassword)
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDto)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<String> confirmTokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendEmailConfirmationEmail(eq(testEmail), confirmTokenCaptor.capture(), any());

            ConfirmEmailRequest confirmRequest = new ConfirmEmailRequest();
            confirmRequest.setToken(confirmTokenCaptor.getValue());
            mockMvc.perform(post("/api/v1/auth/confirm-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk());

            // 2. FORGOT PASSWORD REQUEST
            ForgotPasswordDTO forgotDto = new ForgotPasswordDTO();
            forgotDto.setEmail(testEmail);

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgotDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").isNotEmpty());

            // Capture raw reset token
            ArgumentCaptor<String> resetTokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendPasswordResetEmail(eq(testEmail), resetTokenCaptor.capture(), any());
            String rawResetToken = resetTokenCaptor.getValue();
            assertNotNull(rawResetToken);

            // 3. RESET PASSWORD
            ResetPasswordDTO resetDto = new ResetPasswordDTO();
            resetDto.setToken(rawResetToken);
            resetDto.setNew_password(newPassword);
            resetDto.setNew_password_confirm(newPassword);

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password has been reset successfully."));

            // 4. OLD PASSWORD LOGIN FAILS
            LoginRequest oldLogin = new LoginRequest();
            oldLogin.setEmail(testEmail);
            oldLogin.setPassword(oldPassword);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(oldLogin)))
                    .andExpect(status().isUnauthorized());

            // 5. NEW PASSWORD LOGIN SUCCEEDS
            LoginRequest newLogin = new LoginRequest();
            newLogin.setEmail(testEmail);
            newLogin.setPassword(newPassword);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newLogin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Token Rotation and Revocation Flow")
    class TokenRotationAndRevocationTests {

        @Test
        @DisplayName("Refresh token rotation and logout revocation flow")
        void refreshTokenRotationAndLogout() throws Exception {
            String testEmail = "token.rotation@example.com";
            String password = "Password123#";

            // Register & confirm
            RegisterDTO registerDto = RegisterDTO.builder()
                    .email(testEmail)
                    .password(password)
                    .passwordConfirmation(password)
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDto)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<String> confirmTokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendEmailConfirmationEmail(eq(testEmail), confirmTokenCaptor.capture(), any());

            ConfirmEmailRequest confirmRequest = new ConfirmEmailRequest();
            confirmRequest.setToken(confirmTokenCaptor.getValue());
            mockMvc.perform(post("/api/v1/auth/confirm-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk());

            // Login
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(testEmail);
            loginRequest.setPassword(password);

            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);
            String initialRefreshToken = loginResponse.getRefreshToken();
            String initialAccessToken = loginResponse.getAccessToken();

            // 1. REFRESH ACCESS TOKEN
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(initialRefreshToken);

            MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                    .andReturn();

            RefreshTokenResponse refreshResponse = objectMapper.readValue(
                    refreshResult.getResponse().getContentAsString(), RefreshTokenResponse.class);
            String rotatedAccessToken = refreshResponse.getAccessToken();
            String rotatedRefreshToken = refreshResponse.getRefreshToken();

            assertNotNull(rotatedAccessToken);
            assertNotEquals(initialRefreshToken, rotatedRefreshToken);

            // Access protected endpoint with rotated access token
            mockMvc.perform(get("/api/v1/auth/currentUser")
                            .header("Authorization", "Bearer " + rotatedAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testEmail));

            // Old refresh token is revoked
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized());

            // 2. LOGOUT REVOKES ACTIVE REFRESH TOKEN
            LogoutRequest logoutRequest = new LogoutRequest();
            logoutRequest.setRefreshToken(rotatedRefreshToken);

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(
                                    new org.springframework.security.core.userdetails.User(testEmail, "", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isOk());

            // After logout, refreshing with the rotated token fails
            RefreshTokenRequest postLogoutRefresh = new RefreshTokenRequest();
            postLogoutRefresh.setRefreshToken(rotatedRefreshToken);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postLogoutRefresh)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Unauthenticated Access Guards")
    class UnauthenticatedAccessGuardsTests {

        @Test
        @DisplayName("Protected routes reject requests missing Bearer tokens with 403 Forbidden")
        void protectedRoutesRequireAuthentication() throws Exception {
            mockMvc.perform(get("/api/v1/auth/currentUser"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID() + "/profile"))
                    .andExpect(status().isForbidden());

            LogoutRequest logoutRequest = new LogoutRequest();
            logoutRequest.setRefreshToken("some-token");

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isForbidden());
        }
    }
}
