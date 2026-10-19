package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.dto.UserAssignmentDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Role-Based Access Control & Assignment Workflow Integration Tests")
class UserRoleAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommitteeRepository committeeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtUtil jwtUtil;

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

    private Committee testCommittee;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        // 1. Regular User
        regularUser = User.builder()
                .email("regular.user@example.com")
                .name("Regular User")
                .phoneNumber("01011113333")
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
                .email("committee.board@example.com")
                .name("Committee Board Leader")
                .phoneNumber("01044446666")
                .isAlexEngStudent(true)
                .role(Role.ACM_COMMITTEE_BOARD)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        committeeBoardUser = userRepository.save(committeeBoardUser);
        committeeBoardToken = jwtUtil.generateToken(committeeBoardUser.getEmail(), committeeBoardUser.getRole().name());

        // 3. Super Admin
        superAdmin = User.builder()
                .email("super.admin@example.com")
                .name("Super Administrator")
                .phoneNumber("01099990000")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        superAdmin = userRepository.save(superAdmin);
        superAdminToken = jwtUtil.generateToken(superAdmin.getEmail(), superAdmin.getRole().name());

        // 4. Test Committee
        testCommittee = new Committee();
        testCommittee.setName("Operations Committee");
        testCommittee.setDescription("Logistics and event operations");
        testCommittee = committeeRepository.save(testCommittee);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Role Hierarchy & Endpoint Authorization Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Unauthenticated request to admin endpoints is rejected with 403")
        void rejectsUnauthenticatedAccess() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/v1/admin/users/" + regularUser.getId() + "/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("role", Role.ACM_MEMBER))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user without elevated role is rejected with 403 on all admin endpoints")
        void rejectsRegularUserOnAdminEndpoints() throws Exception {
            // Cannot search users
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());

            // Cannot update roles
            mockMvc.perform(put("/api/v1/admin/users/" + regularUser.getId() + "/role")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("role", Role.ACM_MEMBER))))
                    .andExpect(status().isForbidden());

            // Cannot assign users
            UserAssignmentDto assignmentDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .committeeId(testCommittee.getId())
                    .build();

            mockMvc.perform(post("/api/v1/admin/users/" + regularUser.getId() + "/assign")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assignmentDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Committee Board member can search users but cannot modify user roles (SUPER_ADMIN only)")
        void committeeBoardRoleRestrictions() throws Exception {
            // Committee board can search users
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + committeeBoardToken))
                    .andExpect(status().isOk());

            // Committee board CANNOT update user roles (restricted to SUPER_ADMIN)
            mockMvc.perform(put("/api/v1/admin/users/" + regularUser.getId() + "/role")
                            .header("Authorization", "Bearer " + committeeBoardToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("role", Role.ACM_MEMBER))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Role Assignment & Privilege Elevation Lifecycle")
    class RoleElevationTests {

        @Test
        @DisplayName("Complete flow: regular user denied -> super admin updates role -> new token permits admin access -> assign to committee")
        void fullRoleElevationLifecycle() throws Exception {
            // 1. Initial attempt: regular user denied access
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());

            // 2. Super Admin updates regular user's role to ACM_COMMITTEE_BOARD
            mockMvc.perform(put("/api/v1/admin/users/" + regularUser.getId() + "/role")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("role", Role.ACM_COMMITTEE_BOARD))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ACM_COMMITTEE_BOARD"));

            entityManager.flush();
            entityManager.clear();

            // Verify role in database
            User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
            assertEquals(Role.ACM_COMMITTEE_BOARD, updatedUser.getRole());

            // 3. User issues a new JWT token reflecting the elevated role
            String elevatedToken = jwtUtil.generateToken(updatedUser.getEmail(), updatedUser.getRole().name());

            // 4. Elevated user now has access to admin endpoints
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + elevatedToken))
                    .andExpect(status().isOk());

            // 5. Super Admin assigns user to a specific committee
            UserAssignmentDto assignment = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_COMMITTEE_BOARD)
                    .committeeId(testCommittee.getId())
                    .boardRole("Operations Lead")
                    .build();

            mockMvc.perform(post("/api/v1/admin/users/" + regularUser.getId() + "/assign")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assignment)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            // Verify committee assignment in database
            User fullyAssigned = userRepository.findById(regularUser.getId()).orElseThrow();
            assertEquals(testCommittee.getId(), fullyAssigned.getCommittee().getId());
        }
    }
}
