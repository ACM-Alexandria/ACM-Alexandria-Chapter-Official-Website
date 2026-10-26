package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Admin insights: overview, section tabs and access")
class InsightsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);
        adminToken = tokenFor(saveUser("insights.admin@example.com", Role.SUPER_ADMIN));
        memberToken = tokenFor(saveUser("insights.member@example.com", Role.ACM_MEMBER));
        entityManager.flush();
    }

    private User saveUser(String email, Role role) {
        return userRepository.save(User.builder()
                .email(email)
                .name(email)
                .phoneNumber("01012345678")
                .isAlexEngStudent(false)
                .role(role)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build());
    }

    private String tokenFor(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @Test
    @DisplayName("overview counts members and returns both charts for every period")
    void overviewWorksForEveryPeriod() throws Exception {
        for (String range : List.of("7d", "30d", "90d", "all")) {
            mockMvc.perform(get("/api/admin/insights/overview").param("range", range)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.kpis[0].key", equalTo("members")))
                    .andExpect(jsonPath("$.kpis[0].value", equalTo((int) userRepository.count())))
                    .andExpect(jsonPath("$.kpis", hasSize(5)))
                    .andExpect(jsonPath("$.charts", hasSize(3)))
                    .andExpect(jsonPath("$.openNow").isArray());
        }
    }

    @Test
    @DisplayName("user growth ends at the total number of members")
    void userGrowthEndsAtTotalMembers() throws Exception {
        mockMvc.perform(get("/api/admin/insights/overview").param("range", "all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.charts[1].key", equalTo("userGrowth")))
                .andExpect(jsonPath("$.charts[1].series[0].points[-1:].value", org.hamcrest.Matchers.contains((int) userRepository.count())));
    }

    @Test
    @DisplayName("every section tab loads for every period")
    void everySectionWorksForEveryPeriod() throws Exception {
        for (String section : InsightsService.SECTIONS) {
            for (String range : List.of("7d", "30d", "90d", "all")) {
                mockMvc.perform(get("/api/admin/insights/" + section).param("range", range)
                                .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", equalTo(section)))
                        .andExpect(jsonPath("$.kpis").isArray());
            }
        }
    }

    @Test
    @DisplayName("communication shows the site email state")
    void communicationShowsEmailState() throws Exception {
        mockMvc.perform(get("/api/admin/insights/communication")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statuses[0].label", equalTo("Site emails")));
    }

    @Test
    @DisplayName("an unknown section is a 404")
    void unknownSectionIsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/insights/nope")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ACM members cannot read insights")
    void membersAreForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/insights/overview")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }
}
