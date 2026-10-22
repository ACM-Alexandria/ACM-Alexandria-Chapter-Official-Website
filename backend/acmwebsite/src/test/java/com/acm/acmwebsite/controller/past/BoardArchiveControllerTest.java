package com.acm.acmwebsite.controller.past;

import com.acm.acmwebsite.core.config.JwtFilter;
import com.acm.acmwebsite.core.config.SecurityConfig;
import com.acm.acmwebsite.core.ratelimit.RateLimiterService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.past.BoardArchiveController;
import com.acm.acmwebsite.feature.dto.past.BoardArchiveSummary;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.service.past.BoardArchiveService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardArchiveController.class)
@Import({SecurityConfig.class, JwtFilter.class, RegistrationExceptionHandler.class})
class BoardArchiveControllerTest {
    private static final String URL = "/api/admin/boards/archive";
    @Autowired private MockMvc mvc;
    @Autowired private ApplicationContext context;
    @MockitoBean private BoardArchiveService service;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private RateLimiterService rateLimiter;

    @Test
    void anonymousRequestsUseTheExistingForbiddenResponse() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "ACM_MEMBER", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"})
    void rejectsEveryNonSuperAdminRole(String role) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
                        .with(user("visitor").roles(role)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void superAdminReceivesTheArchiveSummary() throws Exception {
        when(service.archive(2026)).thenReturn(BoardArchiveSummary.builder().archivedSeason(2026)
                .newCurrentSeason(2027).highBoardMembers(3).committees(2).committeeBoardMembers(2)
                .clubs(2).clubBoardMembers(2).usersDowngraded(3).build());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
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
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(user("admin").roles("SUPER_ADMIN")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(service);
    }

    @Test
    void conflictIs409WithTheExistingMessageShape() throws Exception {
        when(service.archive(2026)).thenThrow(new BoardArchiveConflictException("Refresh the current season"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"season\":2026}")
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
                    () -> context.getBean(BoardArchiveController.class).archive(null));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
