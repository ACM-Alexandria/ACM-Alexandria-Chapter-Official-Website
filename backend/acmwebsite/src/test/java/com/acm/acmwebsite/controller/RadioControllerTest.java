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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.RadioController;
import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.service.RadioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RadioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("RadioController WebMvc Tests")
class RadioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RadioService radioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/radio/seasons")
    class GetAllSeasonsTests {

        @Test
        @DisplayName("should return 200 OK with paged seasons")
        void shouldReturnPagedSeasons() throws Exception {
            RadioSeasonDto season = new RadioSeasonDto(1L, 1, "https://pic.png", LocalDateTime.now(), List.of());
            when(radioService.getSeasonsByPage(0)).thenReturn(new PageImpl<>(List.of(season), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/api/radio/seasons").param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(1)))
                    .andExpect(jsonPath("$.content[0].seasonNumber", equalTo(1)));

            verify(radioService).getSeasonsByPage(0);
        }
    }

    @Nested
    @DisplayName("GET /api/radio/seasons/{id}")
    class GetSeasonByIdTests {

        @Test
        @DisplayName("should return 200 OK when season found")
        void shouldReturnSeasonWhenFound() throws Exception {
            RadioSeasonDto season = new RadioSeasonDto(1L, 2, "https://pic2.png", LocalDateTime.now(), List.of());
            when(radioService.getSeasonById(1L)).thenReturn(Optional.of(season));

            mockMvc.perform(get("/api/radio/seasons/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.seasonNumber", equalTo(2)));

            verify(radioService).getSeasonById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when season not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(radioService.getSeasonById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/radio/seasons/{id}", 99L))
                    .andExpect(status().isNotFound());

            verify(radioService).getSeasonById(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/radio/seasons/{id}/episodes")
    class GetEpisodesBySeasonTests {

        @Test
        @DisplayName("should return 200 OK with paged episodes via query param")
        void shouldReturnEpisodesByQueryParam() throws Exception {
            RadioEpisodeDto ep = new RadioEpisodeDto(10L, 1, "Episode 1", "https://img.png", "https://yt.com/1", "Guest A", "Host B", 1L);
            when(radioService.getEpisodesBySeason(1L, 0)).thenReturn(new PageImpl<>(List.of(ep), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/api/radio/seasons/{id}/episodes", 1L).param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(10)))
                    .andExpect(jsonPath("$.content[0].title", equalTo("Episode 1")));

            verify(radioService).getEpisodesBySeason(1L, 0);
        }

        @Test
        @DisplayName("should return 200 OK with paged episodes via path variable")
        void shouldReturnEpisodesByPathVariable() throws Exception {
            RadioEpisodeDto ep = new RadioEpisodeDto(10L, 1, "Episode 1", "https://img.png", "https://yt.com/1", "Guest A", "Host B", 1L);
            when(radioService.getEpisodesBySeason(1L, 2)).thenReturn(new PageImpl<>(List.of(ep), PageRequest.of(2, 10), 1));

            mockMvc.perform(get("/api/radio/seasons/{id}/episodes/page/{page}", 1L, 2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(10)));

            verify(radioService).getEpisodesBySeason(1L, 2);
        }
    }

    @Nested
    @DisplayName("POST /api/radio/seasons")
    class CreateSeasonTests {

        @Test
        @DisplayName("should return 201 Created on season creation")
        void shouldCreateSeasonSuccessfully() throws Exception {
            RadioSeasonDto input = new RadioSeasonDto(null, 3, "https://pic3.png", null, null);
            RadioSeasonDto created = new RadioSeasonDto(3L, 3, "https://pic3.png", LocalDateTime.now(), List.of());

            when(radioService.createSeason(any(RadioSeasonDto.class))).thenReturn(created);

            mockMvc.perform(post("/api/radio/seasons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(3)))
                    .andExpect(jsonPath("$.seasonNumber", equalTo(3)));

            verify(radioService).createSeason(any(RadioSeasonDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/radio/seasons/{id}")
    class UpdateSeasonTests {

        @Test
        @DisplayName("should return 200 OK on season update")
        void shouldUpdateSeasonSuccessfully() throws Exception {
            RadioSeasonDto input = new RadioSeasonDto(null, 4, "https://pic4.png", null, null);
            RadioSeasonDto updated = new RadioSeasonDto(1L, 4, "https://pic4.png", LocalDateTime.now(), List.of());

            when(radioService.updateSeason(eq(1L), any(RadioSeasonDto.class))).thenReturn(updated);

            mockMvc.perform(put("/api/radio/seasons/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.seasonNumber", equalTo(4)));

            verify(radioService).updateSeason(eq(1L), any(RadioSeasonDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/radio/seasons/{id}")
    class DeleteSeasonTests {

        @Test
        @DisplayName("should return 204 No Content on season deletion")
        void shouldDeleteSeasonSuccessfully() throws Exception {
            doNothing().when(radioService).deleteSeason(1L);

            mockMvc.perform(delete("/api/radio/seasons/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(radioService).deleteSeason(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/radio/episodes")
    class CreateEpisodeTests {

        @Test
        @DisplayName("should return 201 Created on episode creation")
        void shouldCreateEpisodeSuccessfully() throws Exception {
            RadioEpisodeDto input = new RadioEpisodeDto(null, 1, "Ep 1", "https://img.png", "https://yt.com", "Guest", "Host", 1L);
            RadioEpisodeDto created = new RadioEpisodeDto(10L, 1, "Ep 1", "https://img.png", "https://yt.com", "Guest", "Host", 1L);

            when(radioService.createEpisode(any(RadioEpisodeDto.class))).thenReturn(created);

            mockMvc.perform(post("/api/radio/episodes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.title", equalTo("Ep 1")));

            verify(radioService).createEpisode(any(RadioEpisodeDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/radio/episodes/{id}")
    class UpdateEpisodeTests {

        @Test
        @DisplayName("should return 200 OK on episode update")
        void shouldUpdateEpisodeSuccessfully() throws Exception {
            RadioEpisodeDto input = new RadioEpisodeDto(null, 1, "Ep 1 Updated", "https://img.png", "https://yt.com", "Guest", "Host", 1L);
            RadioEpisodeDto updated = new RadioEpisodeDto(10L, 1, "Ep 1 Updated", "https://img.png", "https://yt.com", "Guest", "Host", 1L);

            when(radioService.updateEpisode(eq(10L), any(RadioEpisodeDto.class))).thenReturn(updated);

            mockMvc.perform(put("/api/radio/episodes/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.title", equalTo("Ep 1 Updated")));

            verify(radioService).updateEpisode(eq(10L), any(RadioEpisodeDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/radio/episodes/{id}")
    class DeleteEpisodeTests {

        @Test
        @DisplayName("should return 204 No Content on episode deletion")
        void shouldDeleteEpisodeSuccessfully() throws Exception {
            doNothing().when(radioService).deleteEpisode(10L);

            mockMvc.perform(delete("/api/radio/episodes/{id}", 10L))
                    .andExpect(status().isNoContent());

            verify(radioService).deleteEpisode(10L);
        }
    }
}
