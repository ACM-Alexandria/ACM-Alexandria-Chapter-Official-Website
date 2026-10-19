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
import com.acm.acmwebsite.feature.controller.GalleryController;
import com.acm.acmwebsite.feature.entity.GalleryImage;
import com.acm.acmwebsite.feature.service.GalleryService;
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

@WebMvcTest(controllers = GalleryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("GalleryController WebMvc Tests")
class GalleryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GalleryService galleryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/gallery")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 OK with list of gallery images")
        void shouldReturnAllGalleryImages() throws Exception {
            GalleryImage img = new GalleryImage(1L, "https://cloudinary.com/gallery1.png", "Hackathon 2026");
            when(galleryService.getAll()).thenReturn(List.of(img));

            mockMvc.perform(get("/api/gallery"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].imageUrl", equalTo("https://cloudinary.com/gallery1.png")))
                    .andExpect(jsonPath("$[0].caption", equalTo("Hackathon 2026")));

            verify(galleryService).getAll();
        }
    }

    @Nested
    @DisplayName("POST /api/gallery")
    class AddTests {

        @Test
        @DisplayName("should return 201 Created with saved gallery image")
        void shouldAddGalleryImageSuccessfully() throws Exception {
            GalleryImage input = new GalleryImage(null, "https://cloudinary.com/new.png", "Opening Ceremony");
            GalleryImage saved = new GalleryImage(10L, "https://cloudinary.com/new.png", "Opening Ceremony");

            when(galleryService.add(any(GalleryImage.class))).thenReturn(saved);

            mockMvc.perform(post("/api/gallery")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.imageUrl", equalTo("https://cloudinary.com/new.png")))
                    .andExpect(jsonPath("$.caption", equalTo("Opening Ceremony")));

            verify(galleryService).add(any(GalleryImage.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/gallery/{id}")
    class UpdateTests {

        @Test
        @DisplayName("should return 200 OK with updated gallery image")
        void shouldUpdateGalleryImageSuccessfully() throws Exception {
            GalleryImage input = new GalleryImage(null, "https://cloudinary.com/updated.png", "Updated Caption");
            GalleryImage updated = new GalleryImage(1L, "https://cloudinary.com/updated.png", "Updated Caption");

            when(galleryService.update(eq(1L), any(GalleryImage.class))).thenReturn(updated);

            mockMvc.perform(put("/api/gallery/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.caption", equalTo("Updated Caption")));

            verify(galleryService).update(eq(1L), any(GalleryImage.class));
        }

        @Test
        @DisplayName("should return 404 Not Found when update throws RuntimeException")
        void shouldReturn404WhenUpdateFails() throws Exception {
            GalleryImage input = new GalleryImage(null, "https://cloudinary.com/updated.png", "Updated Caption");

            when(galleryService.update(eq(99L), any(GalleryImage.class)))
                    .thenThrow(new RuntimeException("Gallery image not found with id: 99"));

            mockMvc.perform(put("/api/gallery/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Gallery image not found with id: 99"));

            verify(galleryService).update(eq(99L), any(GalleryImage.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/gallery/{id}")
    class DeleteTests {

        @Test
        @DisplayName("should return 204 No Content on successful deletion")
        void shouldDeleteGalleryImageSuccessfully() throws Exception {
            doNothing().when(galleryService).delete(1L);

            mockMvc.perform(delete("/api/gallery/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(galleryService).delete(1L);
        }
    }
}
