package com.acm.acmwebsite.controller.past;

import com.acm.acmwebsite.core.config.JwtFilter;
import com.acm.acmwebsite.core.config.SecurityConfig;
import com.acm.acmwebsite.core.ratelimit.RateLimiterService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.past.BoardController;
import com.acm.acmwebsite.feature.dto.past.PastBoardResponse;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeResponse;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.dto.past.PastClubResponse;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.PastBoardQueryService;
import com.acm.acmwebsite.feature.service.past.PastSeasonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@Import({SecurityConfig.class, JwtFilter.class, RegistrationExceptionHandler.class})
class BoardControllerTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private SystemSettingsService settings;
    @MockitoBean private PastSeasonService seasons;
    @MockitoBean private PastBoardQueryService boards;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private RateLimiterService rateLimiter;

    @Test
    void anonymousSeasonReadsIncludeTheFreshCurrentSeason() throws Exception {
        when(settings.getCurrentSeason()).thenReturn(2026, 2027);
        when(seasons.getSeasons()).thenReturn(List.of(2025, 2024));
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
        when(boards.getBySeason(2025)).thenReturn(PastBoardResponse.builder().season(2025)
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
    void unknownSeasonUsesTheShared404MessageShape() throws Exception {
        when(boards.getBySeason(2024)).thenThrow(new ResourceNotFoundException("Past boards not found"));
        mvc.perform(get("/api/boards/past/2024")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Past boards not found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "2147483648", "2025.5"})
    void malformedSeasonReturns400(String season) throws Exception {
        mvc.perform(get("/api/boards/past/" + season)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(boards);
    }

    @Test
    void invalidSeasonUsesTheShared400MessageShape() throws Exception {
        when(boards.getBySeason(1999)).thenThrow(new IllegalArgumentException("Season must be at least 2000"));
        mvc.perform(get("/api/boards/past/1999")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Season must be at least 2000"));
    }
}
