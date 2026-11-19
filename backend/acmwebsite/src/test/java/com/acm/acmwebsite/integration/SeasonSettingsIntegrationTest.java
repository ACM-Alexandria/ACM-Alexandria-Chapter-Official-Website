package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.badges.BadgeAward;
import com.acm.acmwebsite.feature.badges.BadgeAwardRepository;
import com.acm.acmwebsite.feature.badges.BadgeTypeRepository;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.seasons.SeasonPeriodRepository;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Season settings: the chapter's seasons and quarters")
class SeasonSettingsIntegrationTest {

    private static final String PERIODS = "/api/admin/seasons/periods";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CommitteeRepository committeeRepository;
    @Autowired private SeasonPeriodRepository periodRepository;
    @Autowired private BadgeTypeRepository badgeTypeRepository;
    @Autowired private BadgeAwardRepository awardRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private EmailService emailService;
    @MockitoBean private EmailExitanceService emailExitanceService;
    @MockitoBean private GoogleSheetsService googleSheetsService;

    private User superAdmin;
    private User highBoard;
    private User clubBoard;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);
        superAdmin = saveUser("super@example.com", Role.SUPER_ADMIN, null);
        highBoard = saveUser("high.board@example.com", Role.ACM_HIGH_BOARD, null);
        clubBoard = saveUser("club.board@example.com", Role.ACM_CLUB_BOARD, null);
        // Start from an empty calendar (the app creates a current season at startup; this test's
        // transaction rolls the deletion back afterwards).
        awardRepository.deleteAll();
        periodRepository.deleteAll();
        entityManager.flush();
    }

    private User saveUser(String email, Role role, Committee committee) {
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

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                               User user, Object body) throws Exception {
        return mockMvc.perform(request.header("Authorization", bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, Object> period(String type, String name, String start, String end, int season) {
        return name == null
                ? Map.of("type", type, "startDate", start, "endDate", end, "season", season)
                : Map.of("type", type, "name", name, "startDate", start, "endDate", end, "season", season);
    }

    private Long createdId(ResultActions result) throws Exception {
        return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), PeriodResponse.class).id();
    }

    @Test
    @DisplayName("Dates must fit inside the season and nothing may overlap")
    void periodRules() throws Exception {
        Long seasonId = createdId(send(post(PERIODS), superAdmin, period("SEASON", null, "2026-09-01", "2027-08-31", 2026))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Season 2026")));

        send(post(PERIODS), superAdmin, period("QUARTER", "Q1", "2026-09-01", "2026-11-15", 2026))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Q1 · 2026"));
        // Overlaps Q1.
        send(post(PERIODS), superAdmin, period("QUARTER", "Q2", "2026-11-01", "2027-01-31", 2026)).andExpect(status().isConflict());
        // Runs past the season's end.
        send(post(PERIODS), superAdmin, period("QUARTER", "Q4", "2027-08-01", "2027-09-30", 2026)).andExpect(status().isBadRequest());
        // No such season.
        send(post(PERIODS), superAdmin, period("QUARTER", "Q1", "2030-09-01", "2030-10-01", 2030)).andExpect(status().isBadRequest());
        // A second 2026 season, and a season overlapping this one.
        send(post(PERIODS), superAdmin, period("SEASON", null, "2028-09-01", "2029-08-31", 2026)).andExpect(status().isConflict());
        send(post(PERIODS), superAdmin, period("SEASON", null, "2027-06-01", "2028-05-31", 2027)).andExpect(status().isConflict());
        // Months are automatic; dates must be in order.
        send(post(PERIODS), superAdmin, period("MONTH", null, "2026-09-01", "2026-09-30", 2026)).andExpect(status().isBadRequest());
        send(post(PERIODS), superAdmin, period("SEASON", null, "2028-09-01", "2028-08-01", 2028)).andExpect(status().isBadRequest());

        // Shrinking the season would leave Q1 outside it; a season with quarters can't be deleted.
        send(put(PERIODS + "/" + seasonId), superAdmin, period("SEASON", null, "2026-10-01", "2027-08-31", 2026))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(PERIODS + "/" + seasonId).header("Authorization", bearer(superAdmin)))
                .andExpect(status().isConflict());

        mockMvc.perform(get(PERIODS).header("Authorization", bearer(superAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private static Map<String, Object> ongoing(String type, String name, String start, int season) {
        return name == null
                ? Map.of("type", type, "startDate", start, "season", season)
                : Map.of("type", type, "name", name, "startDate", start, "season", season);
    }

    private String endDateOf(Long id) {
        var end = periodRepository.findById(id).orElseThrow().getEndDate();
        return end == null ? null : end.toString();
    }

    @Test
    @DisplayName("The current season and quarter can be ongoing; adding the next one ends them the day before")
    void ongoingPeriods() throws Exception {
        Long season2026 = createdId(send(post(PERIODS), superAdmin, ongoing("SEASON", null, "2026-09-01", 2026))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endDate").doesNotExist()));
        Long q1 = createdId(send(post(PERIODS), superAdmin, ongoing("QUARTER", "Q1", "2026-09-01", 2026))
                .andExpect(status().isCreated()));

        // The next quarter ends Q1 the day before it starts.
        Long q2 = createdId(send(post(PERIODS), superAdmin, ongoing("QUARTER", "Q2", "2026-11-16", 2026))
                .andExpect(status().isCreated()));
        entityManager.flush();
        entityManager.clear();
        org.junit.jupiter.api.Assertions.assertEquals("2026-11-15", endDateOf(q1));
        org.junit.jupiter.api.Assertions.assertNull(endDateOf(q2));

        // A quarter can't start before its season.
        send(post(PERIODS), superAdmin, ongoing("QUARTER", "Q0", "2026-08-01", 2026)).andExpect(status().isBadRequest());

        // The next season ends this one and its ongoing quarter the day before.
        send(post(PERIODS), superAdmin, ongoing("SEASON", null, "2027-09-01", 2027)).andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();
        org.junit.jupiter.api.Assertions.assertEquals("2027-08-31", endDateOf(season2026));
        org.junit.jupiter.api.Assertions.assertEquals("2027-08-31", endDateOf(q2));

        // Season 2026 has ended, so a new quarter in it needs an end date inside it.
        send(post(PERIODS), superAdmin, ongoing("QUARTER", "Q3", "2027-06-01", 2026)).andExpect(status().isBadRequest());
        // An ongoing season can't start before a later one.
        send(post(PERIODS), superAdmin, ongoing("SEASON", null, "2025-09-01", 2025)).andExpect(status().isConflict());
        // A season that ended can be reopened only if nothing comes after it.
        send(put(PERIODS + "/" + season2026), superAdmin, ongoing("SEASON", null, "2026-09-01", 2026)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Only the super admin changes the calendar; every admin role can read it")
    void access() throws Exception {
        send(post(PERIODS), highBoard, period("SEASON", null, "2026-09-01", "2027-08-31", 2026)).andExpect(status().isForbidden());
        send(post(PERIODS), clubBoard, period("SEASON", null, "2026-09-01", "2027-08-31", 2026)).andExpect(status().isForbidden());

        Long seasonId = createdId(send(post(PERIODS), superAdmin, period("SEASON", null, "2026-09-01", "2027-08-31", 2026))
                .andExpect(status().isCreated()));
        send(put(PERIODS + "/" + seasonId), highBoard, period("SEASON", null, "2026-09-01", "2027-07-31", 2026))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(PERIODS + "/" + seasonId).header("Authorization", bearer(clubBoard)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(PERIODS).header("Authorization", bearer(clubBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        send(post("/api/admin/seasons/months"), clubBoard, Map.of("month", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Nov 2026"))
                .andExpect(jsonPath("$.season").value(2026));
    }

    @Test
    @DisplayName("The current season is the latest season, with its dates")
    void currentSeason() throws Exception {
        send(post(PERIODS), superAdmin, period("SEASON", null, "2025-09-01", "2026-08-31", 2025)).andExpect(status().isCreated());
        send(post(PERIODS), superAdmin, ongoing("SEASON", null, "2026-09-01", 2026)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/seasons/current").header("Authorization", bearer(highBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.season").value(2026))
                .andExpect(jsonPath("$.label").value("Season 2026"))
                .andExpect(jsonPath("$.period.startDate").value("2026-09-01"))
                .andExpect(jsonPath("$.period.endDate").doesNotExist());
    }

    @Test
    @DisplayName("A period badges were awarded for can't be deleted")
    void periodInUse() throws Exception {
        Committee tech = new Committee();
        tech.setName("Technical");
        tech.setDescription("Technical committee");
        tech = committeeRepository.save(tech);
        User member = saveUser("member@example.com", Role.ACM_MEMBER, tech);

        Long seasonId = createdId(send(post(PERIODS), superAdmin, period("SEASON", null, "2026-09-01", "2027-08-31", 2026))
                .andExpect(status().isCreated()));
        BadgeAward award = new BadgeAward();
        award.setBadgeType(badgeTypeRepository.findByName("Season Champion").orElseThrow());
        award.setPeriod(periodRepository.findById(seasonId).orElseThrow());
        award.setMember(member);
        award.setCommittee(tech);
        award.setAwardedBy(superAdmin);
        awardRepository.saveAndFlush(award);

        mockMvc.perform(delete(PERIODS + "/" + seasonId).header("Authorization", bearer(superAdmin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("in use")));
    }
}
