package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import com.acm.acmwebsite.feature.controller.HighBoardController;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.service.HighBoardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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

@WebMvcTest(controllers = HighBoardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("HighBoardController WebMvc Tests")
class HighBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HighBoardService highBoardService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private HighBoard createSampleMember(Long id, String role, Integer order) {
        HighBoard hb = new HighBoard();
        hb.setId(id);
        hb.setRole(role);
        hb.setOrder(order);
        return hb;
    }

    @Nested
    @DisplayName("GET /api/highboard")
    class GetHighBoardTests {

        @Test
        @DisplayName("should return 200 OK with list of high board members")
        void shouldReturnHighBoardMembers() throws Exception {
            HighBoard member = createSampleMember(1L, "Chairperson", 1);
            when(highBoardService.getHighBoard()).thenReturn(List.of(member));

            mockMvc.perform(get("/api/highboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].role", equalTo("Chairperson")))
                    .andExpect(jsonPath("$[0].order", equalTo(1)));

            verify(highBoardService).getHighBoard();
        }
    }

    @Nested
    @DisplayName("POST /api/highboard/members")
    class AddHighBoardMemberTests {

        @Test
        @DisplayName("should return 200 OK with added member on success")
        void shouldAddMemberSuccessfully() throws Exception {
            HighBoard input = createSampleMember(null, "Vice Chair", 2);
            HighBoard saved = createSampleMember(10L, "Vice Chair", 2);

            when(highBoardService.addHighBoardMember(any(HighBoard.class))).thenReturn(saved);

            mockMvc.perform(post("/api/highboard/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.role", equalTo("Vice Chair")));

            verify(highBoardService).addHighBoardMember(any(HighBoard.class));
        }

        @Test
        @DisplayName("should return 400 Bad Request on exception")
        void shouldReturn400OnException() throws Exception {
            HighBoard input = createSampleMember(null, "Vice Chair", 2);
            when(highBoardService.addHighBoardMember(any(HighBoard.class)))
                    .thenThrow(new RuntimeException("Member addition failed"));

            mockMvc.perform(post("/api/highboard/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Member addition failed")));
        }
    }

    @Nested
    @DisplayName("PUT /api/highboard/members/{id}")
    class UpdateHighBoardMemberTests {

        @Test
        @DisplayName("should return 200 OK with updated member on success")
        void shouldUpdateMemberSuccessfully() throws Exception {
            HighBoard input = createSampleMember(null, "Updated Role", 3);
            HighBoard updated = createSampleMember(5L, "Updated Role", 3);

            when(highBoardService.updateHighBoardMember(eq(5L), any(HighBoard.class))).thenReturn(updated);

            mockMvc.perform(put("/api/highboard/members/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.role", equalTo("Updated Role")));

            verify(highBoardService).updateHighBoardMember(eq(5L), any(HighBoard.class));
        }

        @Test
        @DisplayName("should return 404 Not Found on EntityNotFoundException")
        void shouldReturn404WhenNotFound() throws Exception {
            HighBoard input = createSampleMember(null, "Role", 1);
            when(highBoardService.updateHighBoardMember(eq(999L), any(HighBoard.class)))
                    .thenThrow(new EntityNotFoundException("HighBoard member not found"));

            mockMvc.perform(put("/api/highboard/members/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            HighBoard input = createSampleMember(null, "Role", 1);
            when(highBoardService.updateHighBoardMember(eq(1L), any(HighBoard.class)))
                    .thenThrow(new RuntimeException("Update failed"));

            mockMvc.perform(put("/api/highboard/members/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Update failed")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/highboard/members/{id}")
    class DeleteHighBoardMemberTests {

        @Test
        @DisplayName("should return 200 OK when deleted successfully")
        void shouldDeleteMemberSuccessfully() throws Exception {
            doNothing().when(highBoardService).deleteHighBoardMember(1L);

            mockMvc.perform(delete("/api/highboard/members/{id}", 1L))
                    .andExpect(status().isOk());

            verify(highBoardService).deleteHighBoardMember(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found on EntityNotFoundException")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EntityNotFoundException("HighBoard member not found"))
                    .when(highBoardService).deleteHighBoardMember(999L);

            mockMvc.perform(delete("/api/highboard/members/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            doThrow(new RuntimeException("Delete failed"))
                    .when(highBoardService).deleteHighBoardMember(1L);

            mockMvc.perform(delete("/api/highboard/members/{id}", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Delete failed")));
        }
    }
}
