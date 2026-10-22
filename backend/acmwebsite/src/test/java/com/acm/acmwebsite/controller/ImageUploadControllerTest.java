package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.ImageUploadController;
import com.acm.acmwebsite.feature.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImageUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ImageUploadController WebMvc Tests")
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /api/images/upload")
    class UploadImageTests {

        @Test
        @DisplayName("should return 200 OK with url on successful image upload")
        void shouldUploadImageSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "valid-image-content".getBytes());

            when(imageUploadService.uploadImage(any())).thenReturn("https://cloudinary.com/avatar.png");

            mockMvc.perform(multipart("/api/images/upload").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url", equalTo("https://cloudinary.com/avatar.png")));

            verify(imageUploadService).uploadImage(any());
        }

        @Test
        @DisplayName("should return 400 Bad Request when upload throws IllegalArgumentException")
        void shouldReturn400OnIllegalArgumentException() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

            when(imageUploadService.uploadImage(any()))
                    .thenThrow(new IllegalArgumentException("Image file cannot be empty"));

            mockMvc.perform(multipart("/api/images/upload").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", equalTo("Image file cannot be empty")));

            verify(imageUploadService).uploadImage(any());
        }

        @Test
        @DisplayName("should return 500 Internal Server Error when upload throws general Exception")
        void shouldReturn500OnGeneralException() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "large.png", MediaType.IMAGE_PNG_VALUE, "large-content".getBytes());

            when(imageUploadService.uploadImage(any()))
                    .thenThrow(new RuntimeException("Cloudinary connection failed"));

            mockMvc.perform(multipart("/api/images/upload").file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error", containsString("An unexpected error occurred while processing your image: Cloudinary connection failed")));

            verify(imageUploadService).uploadImage(any());
        }

        @Test
        @DisplayName("should handle null file gracefully and return 400 Bad Request")
        void shouldHandleNullFileGracefully() throws Exception {
            when(imageUploadService.uploadImage(null)).thenThrow(new IllegalArgumentException("File cannot be null"));
            ImageUploadController controller = new ImageUploadController(imageUploadService);
            ResponseEntity<?> response = controller.uploadImage(null);
            assertEquals(400, response.getStatusCode().value());
        }
    }
}
