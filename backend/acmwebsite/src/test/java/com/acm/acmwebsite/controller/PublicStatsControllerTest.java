package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.PublicStatsController;
import com.acm.acmwebsite.feature.dto.PublicStatsDto;
import com.acm.acmwebsite.feature.service.PublicStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PublicStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("PublicStatsController WebMvc Tests")
class PublicStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicStatsService publicStatsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/stats returns the four totals and lets browsers cache them for a minute")
    void shouldReturnTotals() throws Exception {
        when(publicStatsService.getStats()).thenReturn(new PublicStatsDto(1200, 85, 3400, 640));

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", equalTo(1200)))
                .andExpect(jsonPath("$.members", equalTo(85)))
                .andExpect(jsonPath("$.eventRegistrations", equalTo(3400)))
                .andExpect(jsonPath("$.programRegistrations", equalTo(640)))
                .andExpect(header().string("Cache-Control", containsString("max-age=60")));

        verify(publicStatsService).getStats();
    }
}
