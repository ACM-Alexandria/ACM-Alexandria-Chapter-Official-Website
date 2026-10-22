package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.EmailLockDto;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("System Settings & Administrative Communication Workflow Integration Tests")
class SystemSettingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private EmailExitanceService emailExitanceService;

    @MockitoBean
    private GoogleSheetsService googleSheetsService;

    private User regularUser;
    private User committeeBoardUser;
    private User superAdmin;

    private String regularUserToken;
    private String committeeBoardToken;
    private String superAdminToken;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);
        when(mailSender.createMimeMessage())
                .thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));

        // Ensure emails start in enabled state
        systemSettingsService.setEmailsEnabled(true);

        // 1. Regular User
        regularUser = User.builder()
                .email("regular.member@example.com")
                .name("Regular Member")
                .phoneNumber("01011115555")
                .isAlexEngStudent(true)
                .department(Department.CSED)
                .batch("2026")
                .role(Role.USER)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        regularUser = userRepository.save(regularUser);
        regularUserToken = jwtUtil.generateToken(regularUser.getEmail(), regularUser.getRole().name());

        // 2. Committee Board User
        committeeBoardUser = User.builder()
                .email("board.leader@example.com")
                .name("Board Leader")
                .phoneNumber("01044447777")
                .isAlexEngStudent(true)
                .role(Role.ACM_COMMITTEE_BOARD)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        committeeBoardUser = userRepository.save(committeeBoardUser);
        committeeBoardToken = jwtUtil.generateToken(committeeBoardUser.getEmail(), committeeBoardUser.getRole().name());

        // 3. Super Admin
        superAdmin = User.builder()
                .email("super.sysadmin@example.com")
                .name("Super System Administrator")
                .phoneNumber("01099991111")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        superAdmin = userRepository.save(superAdmin);
        superAdminToken = jwtUtil.generateToken(superAdmin.getEmail(), superAdmin.getRole().name());

        entityManager.flush();
    }

    @Nested
    @DisplayName("Email Lock & Administrative Communication Lifecycle")
    class EmailLockLifecycleTests {

        @Test
        @DisplayName("Complete flow: query status -> super admin locks emails -> dispatch blocked -> super admin unlocks -> preview -> dispatch succeeds")
        void fullEmailLockAndDispatchLifecycle() throws Exception {
            // 1. QUERY INITIAL EMAIL LOCK STATUS
            mockMvc.perform(get("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + committeeBoardToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled").value(true));

            // 2. SUPER ADMIN LOCKS EMAILS
            EmailLockDto lockRequest = new EmailLockDto(false);

            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lockRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled").value(false));

            entityManager.flush();
            assertFalse(systemSettingsService.isEmailsEnabled());

            // 3. SENDING CUSTOM EMAIL IS BLOCKED WHEN LOCKED
            CustomEmailRequestDto sendRequest = new CustomEmailRequestDto();
            sendRequest.setRecipientFilter("FILTERED_USERS");
            sendRequest.setSubject("Critical Announcement");
            sendRequest.setBody("<p>This is a test notification message.</p>");
            sendRequest.setSelectedUserEmails(List.of(regularUser.getEmail()));

            mockMvc.perform(post("/api/admin/emails/send-custom")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sendRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Emails are locked by the super admin. Unlock them to send emails."));

            // 4. SUPER ADMIN UNLOCKS EMAILS
            EmailLockDto unlockRequest = new EmailLockDto(true);

            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(unlockRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled").value(true));

            entityManager.flush();
            assertTrue(systemSettingsService.isEmailsEnabled());

            // 5. CUSTOM EMAIL PREVIEW
            CustomEmailPreviewRequestDto previewRequest = new CustomEmailPreviewRequestDto();
            previewRequest.setSubject("Critical Announcement");
            previewRequest.setBody("<p>This is a test notification message.</p>");
            previewRequest.setSelectedUserEmails(List.of(regularUser.getEmail()));

            mockMvc.perform(post("/api/admin/emails/preview")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(previewRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailHtml").isNotEmpty())
                    .andExpect(jsonPath("$.totalRecipients").value(1));

            // 6. CUSTOM EMAIL DISPATCH SUCCEEDS WHEN UNLOCKED
            mockMvc.perform(post("/api/admin/emails/send-custom")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sendRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecipients").value(1));
        }
    }

    @Nested
    @DisplayName("Authorization & Role Hierarchy Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Rejects unauthenticated access to system settings and email endpoints")
        void rejectsUnauthenticatedAccess() throws Exception {
            mockMvc.perform(get("/api/admin/settings/email-lock"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new EmailLockDto(false))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/admin/emails/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CustomEmailPreviewRequestDto())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects regular user from accessing settings or sending administrative emails")
        void rejectsRegularUser() throws Exception {
            mockMvc.perform(get("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new EmailLockDto(false))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/admin/emails/send-custom")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CustomEmailRequestDto())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Committee Board member can read email lock but cannot modify it (SUPER_ADMIN only)")
        void committeeBoardCannotModifyEmailLock() throws Exception {
            // Can read
            mockMvc.perform(get("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + committeeBoardToken))
                    .andExpect(status().isOk());

            // Cannot update
            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .header("Authorization", "Bearer " + committeeBoardToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new EmailLockDto(false))))
                    .andExpect(status().isForbidden());
        }
    }
}
