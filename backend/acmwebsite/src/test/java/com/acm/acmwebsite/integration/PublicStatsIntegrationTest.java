package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Public stats: open to signed-out visitors and counted from the database")
class PublicStatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private EmailExitanceService emailExitanceService;

    @MockitoBean
    private GoogleSheetsService googleSheetsService;

    private long usersBefore;
    private long membersBefore;

    @BeforeEach
    void setUp() {
        usersBefore = userRepository.count();
        membersBefore = userRepository.countByRoleIn(List.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD, Role.ACM_CLUB_BOARD, Role.ACM_HIGH_BOARD));
        save("visitor@example.com", Role.USER);
        save("member@example.com", Role.ACM_MEMBER);
        save("committee.head@example.com", Role.ACM_COMMITTEE_BOARD);
        save("admin@example.com", Role.SUPER_ADMIN);
    }

    @Test
    @DisplayName("GET /api/stats works without a token and counts members and boards only")
    void signedOutVisitorGetsTotals() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", equalTo((int) usersBefore + 4)))
                .andExpect(jsonPath("$.members", equalTo((int) membersBefore + 2)))
                .andExpect(jsonPath("$.eventRegistrations").isNumber())
                .andExpect(jsonPath("$.programRegistrations").isNumber());
    }

    private void save(String email, Role role) {
        userRepository.save(User.builder()
                .email(email)
                .name(email)
                .phoneNumber("01012345678")
                .isAlexEngStudent(false)
                .role(role)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build());
    }
}
