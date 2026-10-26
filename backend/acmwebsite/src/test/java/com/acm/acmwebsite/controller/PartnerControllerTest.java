package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.PartnerController;
import com.acm.acmwebsite.feature.entity.Partner;
import com.acm.acmwebsite.feature.service.PartnerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PartnerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("PartnerController WebMvc Tests")
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerService partnerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/partners")
    class GetAllPartnersTests {

        @Test
        @DisplayName("should return 200 OK with list of all partners")
        void shouldReturnAllPartners() throws Exception {
            Partner partner = new Partner(1L, "Google", "https://google.com", "https://img.png");
            when(partnerService.getAllPartners()).thenReturn(List.of(partner));

            mockMvc.perform(get("/api/partners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].name", equalTo("Google")))
                    .andExpect(jsonPath("$[0].website", equalTo("https://google.com")));

            verify(partnerService).getAllPartners();
        }
    }

    @Nested
    @DisplayName("POST /api/partners")
    class CreatePartnerTests {

        @Test
        @DisplayName("should return 201 Created on partner creation")
        void shouldCreatePartnerSuccessfully() throws Exception {
            Partner input = new Partner(null, "Microsoft", "https://microsoft.com", "https://ms.png");
            Partner created = new Partner(10L, "Microsoft", "https://microsoft.com", "https://ms.png");

            when(partnerService.createPartner(any(Partner.class))).thenReturn(created);

            mockMvc.perform(post("/api/partners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.name", equalTo("Microsoft")));

            verify(partnerService).createPartner(any(Partner.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/partners/{id}")
    class UpdatePartnerTests {

        @Test
        @DisplayName("should return 200 OK on partner update")
        void shouldUpdatePartnerSuccessfully() throws Exception {
            Partner input = new Partner(null, "Microsoft Updated", "https://microsoft.com", "https://ms.png");
            Partner updated = new Partner(10L, "Microsoft Updated", "https://microsoft.com", "https://ms.png");

            when(partnerService.updatePartner(eq(10L), any(Partner.class))).thenReturn(updated);

            mockMvc.perform(put("/api/partners/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.name", equalTo("Microsoft Updated")));

            verify(partnerService).updatePartner(eq(10L), any(Partner.class));
        }

        @Test
        @DisplayName("should return 404 Not Found when update throws RuntimeException")
        void shouldReturn404WhenUpdateFails() throws Exception {
            Partner input = new Partner(null, "Microsoft", "https://microsoft.com", "https://ms.png");

            when(partnerService.updatePartner(eq(99L), any(Partner.class)))
                    .thenThrow(new RuntimeException("Partner not found with id: 99"));

            mockMvc.perform(put("/api/partners/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Partner not found with id: 99"));

            verify(partnerService).updatePartner(eq(99L), any(Partner.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/partners/{id}")
    class DeletePartnerTests {

        @Test
        @DisplayName("should return 204 No Content on partner deletion")
        void shouldDeletePartnerSuccessfully() throws Exception {
            doNothing().when(partnerService).deletePartner(1L);

            mockMvc.perform(delete("/api/partners/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(partnerService).deletePartner(1L);
        }
    }
}
