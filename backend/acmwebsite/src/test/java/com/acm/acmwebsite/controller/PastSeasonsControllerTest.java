package com.acm.acmwebsite.controller;

import com.acm.acmwebsite.core.config.JwtFilter;
import com.acm.acmwebsite.core.config.SecurityConfig;
import com.acm.acmwebsite.core.ratelimit.RateLimiterService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.PastSeasonsController;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardArchiveSummary;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeResponse;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.PastSeasonsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PastSeasonsController.class)
@Import({SecurityConfig.class, JwtFilter.class, RegistrationExceptionHandler.class})
class PastSeasonsControllerTest {
    private static final String ARCHIVE_URL = "/api/admin/boards/archive";
    @Autowired private MockMvc mvc;
    @Autowired private ApplicationContext context;
    @MockitoBean private SystemSettingsService settings;
    @MockitoBean private PastSeasonsService service;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private RateLimiterService rateLimiter;

    // ----- public board views -----

    @Test
    void anonymousSeasonReadsIncludeTheFreshCurrentSeason() throws Exception {
        when(settings.getCurrentSeason()).thenReturn(2026, 2027);
        when(service.getSeasons()).thenReturn(List.of(2025, 2024));
        mvc.perform(get("/api/boards/seasons")).andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSeason").value(2026))
                .andExpect(jsonPath("$.pastSeasons[0]").value(2025));
        mvc.perform(get("/api/boards/seasons")).andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSeason").value(2027));
        verify(settings, times(2)).getCurrentSeason();
    }

    @Test
    void anonymousVisitorsCanReadPastBoards() throws Exception {
        var member = new PastCommitteeBoardResponse();
        member.setId(10L);
        member.setUserName("Committee head");
        member.setRole("Head");
        member.setPastCommitteeId(1L);
        var committee = new PastCommitteeResponse();
        committee.setId(1L);
        committee.setName("Technical");
        committee.setLogoUrl("https://example.org/committee.png");
        committee.setBoardRoles(List.of(member));
        var club = new PastClubResponse();
        club.setId(2L);
        club.setName("Boardless club");
        when(service.getBySeason(2025)).thenReturn(PastBoardResponse.builder().season(2025)
                .highBoard(List.of()).committees(List.of(committee)).clubs(List.of(club)).build());
        mvc.perform(get("/api/boards/past/2025")).andExpect(status().isOk())
                .andExpect(jsonPath("$.season").value(2025))
                .andExpect(jsonPath("$.highBoard").isArray())
                .andExpect(jsonPath("$.committees[0].name").value("Technical"))
                .andExpect(jsonPath("$.committees[0].logoUrl").value(committee.getLogoUrl()))
                .andExpect(jsonPath("$.committees[0].boardRoles[0].userName").value("Committee head"))
                .andExpect(jsonPath("$.committees[0].boardRoles[0].role").value("Head"))
                .andExpect(jsonPath("$.clubs[0].boardRoles").isEmpty());
    }

    @Test
    void unknownSeasonUsesTheSharedNotFoundMessageShape() throws Exception {
        when(service.getBySeason(2024)).thenThrow(new ResourceNotFoundException("Past boards not found"));
        mvc.perform(get("/api/boards/past/2024")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Past boards not found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "2147483648", "2025.5"})
    void malformedSeasonReturns400(String season) throws Exception {
        mvc.perform(get("/api/boards/past/" + season)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(service);
    }

    @Test
    void invalidSeasonUsesTheSharedBadRequestMessageShape() throws Exception {
        when(service.getBySeason(1999)).thenThrow(new IllegalArgumentException("Season must be at least 2000"));
        mvc.perform(get("/api/boards/past/1999")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Season must be at least 2000"));
    }

    // ----- archive -----

    @Test
    void superAdminReceivesTheArchiveSummary() throws Exception {
        when(service.archive(2026)).thenReturn(BoardArchiveSummary.builder().archivedSeason(2026)
                .newCurrentSeason(2027).highBoardMembers(3).committees(2).committeeBoardMembers(2)
                .clubs(2).clubBoardMembers(2).usersDowngraded(3).build());
        mvc.perform(post(ARCHIVE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
                        .with(user("admin").roles("SUPER_ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.archivedSeason").value(2026))
                .andExpect(jsonPath("$.newCurrentSeason").value(2027))
                .andExpect(jsonPath("$.usersDowngraded").value(3))
                .andExpect(jsonPath("$.highBoardMembers").value(3));
        verify(service).archive(2026);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"season\":null}", "{\"season\":1999}", "{"})
    void invalidConfirmationIs400WithAMessage(String body) throws Exception {
        mvc.perform(post(ARCHIVE_URL).contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(user("admin").roles("SUPER_ADMIN")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(service);
    }

    @Test
    void conflictIs409WithTheExistingMessageShape() throws Exception {
        when(service.archive(2026)).thenThrow(new BoardArchiveConflictException("Refresh the current season"));
        mvc.perform(post(ARCHIVE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
                        .with(user("admin").roles("SUPER_ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Refresh the current season"));
    }

    @Test
    void methodSecurityAlsoRejectsDirectCallsByOtherAdmins() {
        var security = SecurityContextHolder.createEmptyContext();
        security.setAuthentication(UsernamePasswordAuthenticationToken.authenticated("board", null,
                List.of(new SimpleGrantedAuthority("ROLE_ACM_HIGH_BOARD"))));
        SecurityContextHolder.setContext(security);
        try {
            assertThrows(AccessDeniedException.class,
                    () -> context.getBean(PastSeasonsController.class).archive(null));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ----- admin endpoints -----

    @Test
    void anonymousRequestsUseTheExistingForbiddenResponse() throws Exception {
        for (var request : allAdminRequests()) {
            mvc.perform(request).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "ACM_MEMBER"})
    void regularMembersCannotUseAnyBoardEndpoint(String role) throws Exception {
        for (var request : allAdminRequests()) {
            mvc.perform(request.with(user("visitor").roles(role))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUPER_ADMIN", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"})
    void boardRolesCanManagePastBoards(String role) throws Exception {
        for (var request : managementRequests()) {
            mvc.perform(request.with(user("board").roles(role)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"})
    void onlySuperAdminCanArchiveTheSeason(String role) throws Exception {
        mvc.perform(post(ARCHIVE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
                        .with(user("board").roles(role)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    private List<MockHttpServletRequestBuilder> allAdminRequests() {
        List<MockHttpServletRequestBuilder> requests = new ArrayList<>(managementRequests());
        requests.add(post(ARCHIVE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}"));
        return requests;
    }

    private List<MockHttpServletRequestBuilder> managementRequests() {
        List<MockHttpServletRequestBuilder> requests = new ArrayList<>();
        requests.add(get("/api/admin/boards/current-season"));
        requests.add(get("/api/admin/boards/past/seasons"));
        for (String resource : List.of("high-board", "committees", "committee-board", "clubs", "club-board")) {
            String path = "/api/admin/boards/past/" + resource;
            requests.add(get(path).param("season", "2025"));
            requests.add(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"));
            requests.add(put(path + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"));
            requests.add(delete(path + "/1"));
        }
        return requests;
    }
}
