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
import com.acm.acmwebsite.feature.controller.SocialLinkController;
import com.acm.acmwebsite.feature.entity.SocialLink;
import com.acm.acmwebsite.feature.service.SocialLinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
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

@WebMvcTest(controllers = SocialLinkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("SocialLinkController WebMvc Tests")
class SocialLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SocialLinkService socialLinkService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/socialLinks")
    class GetSocialLinksTests {

        @Test
        @DisplayName("should return 200 OK with list of all social links")
        void shouldReturnAllSocialLinks() throws Exception {
            SocialLink link = new SocialLink(1L, "https://facebook.com/acm", "FACEBOOK");
            when(socialLinkService.getAllLinks()).thenReturn(List.of(link));

            mockMvc.perform(get("/api/socialLinks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].platform", equalTo("FACEBOOK")))
                    .andExpect(jsonPath("$[0].url", equalTo("https://facebook.com/acm")));

            verify(socialLinkService).getAllLinks();
        }
    }

    @Nested
    @DisplayName("GET /api/socialLinks/{id}")
    class GetSocialLinkByIdTests {

        @Test
        @DisplayName("should return 200 OK when link exists")
        void shouldReturnLinkWhenFound() throws Exception {
            SocialLink link = new SocialLink(1L, "https://linkedin.com/acm", "LINKEDIN");
            when(socialLinkService.getLinkById(1L)).thenReturn(Optional.of(link));

            mockMvc.perform(get("/api/socialLinks/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.platform", equalTo("LINKEDIN")))
                    .andExpect(jsonPath("$.url", equalTo("https://linkedin.com/acm")));

            verify(socialLinkService).getLinkById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when link does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(socialLinkService.getLinkById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/socialLinks/{id}", 99L))
                    .andExpect(status().isNotFound());

            verify(socialLinkService).getLinkById(99L);
        }
    }

    @Nested
    @DisplayName("POST /api/socialLinks")
    class CreateSocialLinkTests {

        @Test
        @DisplayName("should return 200 OK with created social link")
        void shouldCreateSocialLink() throws Exception {
            SocialLink input = new SocialLink(null, "https://instagram.com/acm", "INSTAGRAM");
            SocialLink created = new SocialLink(10L, "https://instagram.com/acm", "INSTAGRAM");

            when(socialLinkService.createSocialLink(any(SocialLink.class))).thenReturn(created);

            mockMvc.perform(post("/api/socialLinks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.platform", equalTo("INSTAGRAM")));

            verify(socialLinkService).createSocialLink(any(SocialLink.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/socialLinks/{id}")
    class UpdateSocialLinkTests {

        @Test
        @DisplayName("should return 200 OK with updated social link")
        void shouldUpdateSocialLinkSuccessfully() throws Exception {
            SocialLink input = new SocialLink(null, "https://youtube.com/acm", "YOUTUBE");
            SocialLink updated = new SocialLink(1L, "https://youtube.com/acm", "YOUTUBE");

            when(socialLinkService.updateSocialLink(eq(1L), any(SocialLink.class))).thenReturn(updated);

            mockMvc.perform(put("/api/socialLinks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.platform", equalTo("YOUTUBE")));

            verify(socialLinkService).updateSocialLink(eq(1L), any(SocialLink.class));
        }

        @Test
        @DisplayName("should return 404 Not Found when update fails with RuntimeException")
        void shouldReturn404WhenUpdateFails() throws Exception {
            SocialLink input = new SocialLink(null, "https://youtube.com/acm", "YOUTUBE");

            when(socialLinkService.updateSocialLink(eq(99L), any(SocialLink.class)))
                    .thenThrow(new RuntimeException("Link not found"));

            mockMvc.perform(put("/api/socialLinks/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Link not found"));

            verify(socialLinkService).updateSocialLink(eq(99L), any(SocialLink.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/socialLinks/{id}")
    class DeleteSocialLinkTests {

        @Test
        @DisplayName("should return 204 No Content on successful deletion")
        void shouldDeleteSocialLink() throws Exception {
            doNothing().when(socialLinkService).deleteSocialLink(1L);

            mockMvc.perform(delete("/api/socialLinks/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(socialLinkService).deleteSocialLink(1L);
        }
    }
}
