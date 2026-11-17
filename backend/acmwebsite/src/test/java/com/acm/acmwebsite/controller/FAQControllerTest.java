package com.acm.acmwebsite.controller;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.FAQController;
import com.acm.acmwebsite.feature.dto.FAQDto;
import com.acm.acmwebsite.feature.service.FAQService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.acm.acmwebsite.feature.enums.FAQCategory.Events;
import static com.acm.acmwebsite.feature.enums.FAQCategory.Joining;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FAQController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FAQControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private FAQService faqService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void getAllFAQs_returnsTheRequestedPage() throws Exception {
        FAQDto faq = dto(1, "How do I join?", "Use the registration form.", Joining, 0);
        when(faqService.getFAQs(eq("join"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(faq)));

        mockMvc.perform(get("/api/faq").param("page", "0").param("size", "10").param("query", "join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].question").value("How do I join?"));

        verify(faqService).getFAQs(eq("join"), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }

    @Test
    void getAllFAQs_rejectsInvalidPagingValues() throws Exception {
        mockMvc.perform(get("/api/faq").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(faqService);
    }

    @Test
    void addFAQ_returnsCreatedEntry() throws Exception {
        FAQDto input = dto(null, "How do I join?", "Use the registration form.", Joining, 0);
        FAQDto created = dto(1, "How do I join?", "Use the registration form.", Joining, 0);
        when(faqService.addFAQ(any(FAQDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/faq").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(faqService).addFAQ(any(FAQDto.class));
    }

    @Test
    void updateFAQ_returnsUpdatedEntry() throws Exception {
        FAQDto update = dto(null, "When is the event?", "See the events page.", Events, 2);
        FAQDto updated = dto(4, "When is the event?", "See the events page.", Events, 2);
        when(faqService.updateFAQ(eq(4), any(FAQDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/faq/4").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Events"))
                .andExpect(jsonPath("$.order").value(2));

        verify(faqService).updateFAQ(eq(4), any(FAQDto.class));
    }

    @Test
    void reorderFAQs_returnsReorderedEntries() throws Exception {
        List<FAQDto> reordered = List.of(dto(2, "Second", "Answer", Joining, 0), dto(1, "First", "Answer", Joining, 1));
        when(faqService.reorderFAQs(anyList())).thenReturn(reordered);

        mockMvc.perform(put("/api/faq/reorder").contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":1,\"order\":1},{\"id\":2,\"order\":0}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));

        verify(faqService).reorderFAQs(anyList());
    }

    @Test
    void deleteFAQ_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/faq/5"))
                .andExpect(status().isNoContent());

        verify(faqService).deleteFAQ(5);
    }

    private FAQDto dto(Integer id, String question, String answer, com.acm.acmwebsite.feature.enums.FAQCategory category, int order) {
        return FAQDto.builder().id(id).question(question).answer(answer).category(category).order(order).build();
    }
}
