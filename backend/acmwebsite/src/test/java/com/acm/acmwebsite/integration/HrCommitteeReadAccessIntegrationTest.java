package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("HR Committee members: read-only access to the HR dashboard data")
class HrCommitteeReadAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    private String hrMemberToken;
    private String otherCommitteeMemberToken;
    private String plainMemberToken;
    private User hrMember;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        Committee hr = new Committee();
        hr.setName("HR Committee");
        hr.setDescription("People and culture");
        hr = committeeRepository.save(hr);

        Committee media = new Committee();
        media.setName("Media Committee");
        media.setDescription("Design and content");
        media = committeeRepository.save(media);

        hrMember = saveMember("hr.member@example.com", Role.ACM_MEMBER, hr);
        User otherCommitteeMember = saveMember("media.member@example.com", Role.ACM_MEMBER, media);
        User plainMember = saveMember("no.committee@example.com", Role.ACM_MEMBER, null);

        hrMemberToken = tokenFor(hrMember);
        otherCommitteeMemberToken = tokenFor(otherCommitteeMember);
        plainMemberToken = tokenFor(plainMember);

        entityManager.flush();
    }

    private User saveMember(String email, Role role, Committee committee) {
        return userRepository.save(User.builder()
                .email(email)
                .name(email)
                .phoneNumber("01012345678")
                .isAlexEngStudent(false)
                .role(role)
                .committee(committee)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build());
    }

    private String tokenFor(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @Test
    @DisplayName("HR member can search users")
    void hrMemberCanSearchUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR member can read the email-lock status")
    void hrMemberCanReadEmailLock() throws Exception {
        mockMvc.perform(get("/api/admin/settings/email-lock").header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR committee name match ignores case")
    void committeeNameMatchIgnoresCase() throws Exception {
        hrMember.getCommittee().setName("hr committee");
        entityManager.flush();

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR member cannot change the email lock (write stays admin-only)")
    void hrMemberCannotWriteEmailLock() throws Exception {
        mockMvc.perform(put("/api/admin/settings/email-lock")
                        .header("Authorization", "Bearer " + hrMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailsEnabled\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HR member cannot change user roles")
    void hrMemberCannotChangeRoles() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + hrMember.getId() + "/role")
                        .header("Authorization", "Bearer " + hrMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Member of another committee gets no access")
    void otherCommitteeMemberIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + otherCommitteeMemberToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/settings/email-lock").header("Authorization", "Bearer " + otherCommitteeMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Member with no committee gets no access")
    void memberWithoutCommitteeIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + plainMemberToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/settings/email-lock").header("Authorization", "Bearer " + plainMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HR member can read a member profile")
    void hrMemberCanReadMemberProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/admin/" + hrMember.getId()).header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Member of another committee cannot read member profiles")
    void otherCommitteeMemberCannotReadMemberProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/admin/" + hrMember.getId()).header("Authorization", "Bearer " + otherCommitteeMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HR member can read a committee's call list")
    void hrMemberCanReadCommitteeCalls() throws Exception {
        mockMvc.perform(get("/api/committee/" + hrMember.getCommittee().getId() + "/calls")
                        .header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR member can open registration analysis (not forbidden)")
    void hrMemberCanOpenRegistrationAnalysis() throws Exception {
        mockMvc.perform(get("/api/events/999999/registrations/analysis")
                        .header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403, result.getResponse().getStatus()));
    }

    @Test
    @DisplayName("HR member cannot sync registration sheets (write stays with the board)")
    void hrMemberCannotSyncRegistrationSheet() throws Exception {
        mockMvc.perform(post("/api/events/999999/registrations/sheet")
                        .header("Authorization", "Bearer " + hrMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Member of another committee cannot read committee calls")
    void otherCommitteeMemberCannotReadCommitteeCalls() throws Exception {
        mockMvc.perform(get("/api/committee/" + hrMember.getCommittee().getId() + "/calls")
                        .header("Authorization", "Bearer " + otherCommitteeMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated requests are still rejected")
    void unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/settings/email-lock")).andExpect(status().isForbidden());
    }
}
