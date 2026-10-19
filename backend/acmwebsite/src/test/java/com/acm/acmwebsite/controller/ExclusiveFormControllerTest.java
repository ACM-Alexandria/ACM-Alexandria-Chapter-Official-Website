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
import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.ExclusiveFormController;
import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.mapper.ExclusiveFormMapper;
import com.acm.acmwebsite.feature.service.ExclusiveFormRegistrationService;
import com.acm.acmwebsite.feature.service.ExclusiveFormService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
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

@WebMvcTest(controllers = ExclusiveFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("ExclusiveFormController WebMvc Tests")
class ExclusiveFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExclusiveFormService exclusiveFormService;

    @MockitoBean
    private ExclusiveFormRegistrationService registrationService;

    @MockitoBean
    private ExclusiveFormMapper mapper;

    @MockitoBean
    private AuthorizationService authorizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms")
    class GetAllFormsTests {

        @Test
        @DisplayName("should return 200 OK with list of all exclusive forms")
        void shouldReturnAllForms() throws Exception {
            ExclusiveForm form = ExclusiveForm.builder().id(1L).title("Form 1").build();
            ExclusiveFormDto dto = ExclusiveFormDto.builder().id(1L).title("Form 1").build();

            when(exclusiveFormService.getAllForms()).thenReturn(List.of(form));
            when(mapper.toDto(form)).thenReturn(dto);

            mockMvc.perform(get("/api/exclusive-forms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].title", equalTo("Form 1")));

            verify(exclusiveFormService).getAllForms();
        }
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms/active")
    class GetActiveFormsTests {

        @Test
        @DisplayName("should return 200 OK with active exclusive forms")
        void shouldReturnActiveForms() throws Exception {
            ExclusiveForm form = ExclusiveForm.builder().id(2L).title("Active Form").isActive(true).build();
            ExclusiveFormDto dto = ExclusiveFormDto.builder().id(2L).title("Active Form").isActive(true).build();

            when(exclusiveFormService.getActiveForms()).thenReturn(List.of(form));
            when(mapper.toDto(form)).thenReturn(dto);

            mockMvc.perform(get("/api/exclusive-forms/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(2)))
                    .andExpect(jsonPath("$[0].title", equalTo("Active Form")));

            verify(exclusiveFormService).getActiveForms();
        }
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms/{id}")
    class GetFormByIdTests {

        @Test
        @DisplayName("should return 200 OK with form DTO when found")
        void shouldReturnFormById() throws Exception {
            ExclusiveForm form = ExclusiveForm.builder().id(1L).title("Form 1").build();
            ExclusiveFormDto dto = ExclusiveFormDto.builder().id(1L).title("Form 1").build();

            when(exclusiveFormService.getFormById(1L)).thenReturn(form);
            when(mapper.toDto(form)).thenReturn(dto);

            mockMvc.perform(get("/api/exclusive-forms/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.title", equalTo("Form 1")));

            verify(exclusiveFormService).getFormById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when form does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(exclusiveFormService.getFormById(99L)).thenThrow(new EntityNotFoundException("Form not found"));

            mockMvc.perform(get("/api/exclusive-forms/{id}", 99L))
                    .andExpect(status().isNotFound());

            verify(exclusiveFormService).getFormById(99L);
        }
    }

    @Nested
    @DisplayName("POST /api/exclusive-forms")
    class CreateFormTests {

        @Test
        @DisplayName("should return 200 OK with saved form DTO on success")
        void shouldCreateFormSuccessfully() throws Exception {
            ExclusiveFormDto inputDto = ExclusiveFormDto.builder().title("New Form").build();
            ExclusiveForm entity = ExclusiveForm.builder().title("New Form").build();
            ExclusiveForm saved = ExclusiveForm.builder().id(1L).title("New Form").build();
            ExclusiveFormDto outputDto = ExclusiveFormDto.builder().id(1L).title("New Form").build();

            when(mapper.toEntity(any(ExclusiveFormDto.class))).thenReturn(entity);
            when(exclusiveFormService.saveForm(entity)).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(outputDto);

            mockMvc.perform(post("/api/exclusive-forms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.title", equalTo("New Form")));

            verify(exclusiveFormService).saveForm(entity);
        }

        @Test
        @DisplayName("should return 400 Bad Request when save throws exception")
        void shouldReturn400WhenSaveFails() throws Exception {
            ExclusiveFormDto inputDto = ExclusiveFormDto.builder().title("New Form").build();

            when(mapper.toEntity(any(ExclusiveFormDto.class))).thenThrow(new RuntimeException("Save failed"));

            mockMvc.perform(post("/api/exclusive-forms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Save failed")));
        }
    }

    @Nested
    @DisplayName("PUT /api/exclusive-forms/{id}")
    class UpdateFormTests {

        @Test
        @DisplayName("should return 200 OK with updated form DTO on success")
        void shouldUpdateFormSuccessfully() throws Exception {
            ExclusiveFormDto inputDto = ExclusiveFormDto.builder().title("Updated").build();
            ExclusiveForm updatedEntity = ExclusiveForm.builder().id(1L).title("Updated").build();
            ExclusiveFormDto outputDto = ExclusiveFormDto.builder().id(1L).title("Updated").build();

            when(exclusiveFormService.updateForm(eq(1L), any(ExclusiveFormDto.class))).thenReturn(updatedEntity);
            when(mapper.toDto(updatedEntity)).thenReturn(outputDto);

            mockMvc.perform(put("/api/exclusive-forms/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", equalTo("Updated")));

            verify(exclusiveFormService).updateForm(eq(1L), any(ExclusiveFormDto.class));
        }

        @Test
        @DisplayName("should return 404 Not Found when updating non-existent form")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            ExclusiveFormDto inputDto = ExclusiveFormDto.builder().title("Updated").build();
            when(exclusiveFormService.updateForm(eq(99L), any(ExclusiveFormDto.class)))
                    .thenThrow(new EntityNotFoundException("Form not found"));

            mockMvc.perform(put("/api/exclusive-forms/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request when update throws general exception")
        void shouldReturn400WhenUpdateThrowsException() throws Exception {
            ExclusiveFormDto inputDto = ExclusiveFormDto.builder().title("Updated").build();
            when(exclusiveFormService.updateForm(eq(1L), any(ExclusiveFormDto.class)))
                    .thenThrow(new RuntimeException("Update failed"));

            mockMvc.perform(put("/api/exclusive-forms/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Update failed")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exclusive-forms/{id}")
    class DeleteFormTests {

        @Test
        @DisplayName("should return 200 OK on successful deletion")
        void shouldDeleteSuccessfully() throws Exception {
            doNothing().when(exclusiveFormService).deleteForm(1L);

            mockMvc.perform(delete("/api/exclusive-forms/{id}", 1L))
                    .andExpect(status().isOk());

            verify(exclusiveFormService).deleteForm(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when deleting non-existent form")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            doThrow(new EntityNotFoundException("Form not found")).when(exclusiveFormService).deleteForm(99L);

            mockMvc.perform(delete("/api/exclusive-forms/{id}", 99L))
                    .andExpect(status().isNotFound());

            verify(exclusiveFormService).deleteForm(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms/{id}/questions")
    class GetFormQuestionsTests {

        @Test
        @DisplayName("should return 200 OK with questions list")
        void shouldReturnQuestions() throws Exception {
            FormQuestionResponseDto q = FormQuestionResponseDto.builder()
                    .id(10L)
                    .questionText("Question 1")
                    .questionType("TEXT")
                    .build();

            when(registrationService.getQuestions(1L)).thenReturn(List.of(q));

            mockMvc.perform(get("/api/exclusive-forms/{id}/questions", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(10)))
                    .andExpect(jsonPath("$[0].question_text", equalTo("Question 1")));

            verify(registrationService).getQuestions(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/exclusive-forms/{id}/questions")
    class CreateFormQuestionTests {

        @Test
        @DisplayName("should return 201 Created with created question DTO")
        void shouldCreateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question 1")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();
            FormQuestionResponseDto resp = FormQuestionResponseDto.builder()
                    .id(10L)
                    .questionText("Question 1")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(registrationService.createQuestion(eq(1L), any(FormQuestionRequestDto.class))).thenReturn(resp);

            mockMvc.perform(post("/api/exclusive-forms/{id}/questions", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.question_text", equalTo("Question 1")));

            verify(registrationService).createQuestion(eq(1L), any(FormQuestionRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/exclusive-forms/{id}/questions/{questionId}")
    class UpdateFormQuestionTests {

        @Test
        @DisplayName("should return 200 OK with updated question DTO")
        void shouldUpdateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question 1 Updated")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();
            FormQuestionResponseDto resp = FormQuestionResponseDto.builder()
                    .id(10L)
                    .questionText("Question 1 Updated")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            when(registrationService.updateQuestion(eq(1L), eq(10L), any(FormQuestionRequestDto.class))).thenReturn(resp);

            mockMvc.perform(put("/api/exclusive-forms/{id}/questions/{questionId}", 1L, 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.question_text", equalTo("Question 1 Updated")));

            verify(registrationService).updateQuestion(eq(1L), eq(10L), any(FormQuestionRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exclusive-forms/{id}/questions/{questionId}")
    class DeleteFormQuestionTests {

        @Test
        @DisplayName("should return 204 No Content on successful deletion")
        void shouldDeleteQuestionSuccessfully() throws Exception {
            doNothing().when(registrationService).deleteQuestion(1L, 10L);

            mockMvc.perform(delete("/api/exclusive-forms/{id}/questions/{questionId}", 1L, 10L))
                    .andExpect(status().isNoContent());

            verify(registrationService).deleteQuestion(1L, 10L);
        }
    }

    @Nested
    @DisplayName("POST /api/exclusive-forms/{id}/register")
    class RegisterTests {

        @Test
        @DisplayName("should return 400 Bad Request when userId is null")
        void shouldReturn400WhenUserIdNull() throws Exception {
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(null);

            mockMvc.perform(post("/api/exclusive-forms/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(post("/api/exclusive-forms/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(post("/api/exclusive-forms/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 201 Created on valid registration")
        void shouldRegisterSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(registrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));

            mockMvc.perform(post("/api/exclusive-forms/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            verify(registrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exclusive-forms/{id}/register/{userId}")
    class CancelRegistrationTests {

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(delete("/api/exclusive-forms/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(delete("/api/exclusive-forms/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 204 No Content on successful cancellation")
        void shouldCancelSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(registrationService).cancelRegistration(userId, 1L);

            mockMvc.perform(delete("/api/exclusive-forms/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isNoContent());

            verify(registrationService).cancelRegistration(userId, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms/{id}/is-registered/{userId}")
    class IsRegisteredForFormTests {

        @Test
        @DisplayName("should return 400 Bad Request on invalid UUID string")
        void shouldReturn400OnInvalidUuid() throws Exception {
            mockMvc.perform(get("/api/exclusive-forms/{id}/is-registered/{userId}", 1L, "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(get("/api/exclusive-forms/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(get("/api/exclusive-forms/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 OK with registered true")
        void shouldReturnRegisteredTrue() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            when(registrationService.isUserRegistered(userId, 1L)).thenReturn(true);

            mockMvc.perform(get("/api/exclusive-forms/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered", equalTo(true)));

            verify(registrationService).isUserRegistered(userId, 1L);
        }

        @Test
        @DisplayName("should return 200 OK with registered false")
        void shouldReturnRegisteredFalse() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            when(registrationService.isUserRegistered(userId, 1L)).thenReturn(false);

            mockMvc.perform(get("/api/exclusive-forms/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered", equalTo(false)));

            verify(registrationService).isUserRegistered(userId, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/exclusive-forms/{id}/registrations/analysis")
    class GetFormRegistrationAnalysisTests {

        @Test
        @DisplayName("should return 200 OK with registration analysis DTO")
        void shouldReturnAnalysis() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(42L)
                    .alexUniStudentCount(30L)
                    .nonAlexUniStudentCount(12L)
                    .build();

            when(registrationService.getRegistrationAnalysis(1L)).thenReturn(analysis);

            mockMvc.perform(get("/api/exclusive-forms/{id}/registrations/analysis", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(42)))
                    .andExpect(jsonPath("$.alexUniStudentCount", equalTo(30)))
                    .andExpect(jsonPath("$.nonAlexUniStudentCount", equalTo(12)));

            verify(registrationService).getRegistrationAnalysis(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/exclusive-forms/{id}/registrations/sheet")
    class SyncFormRegistrationsSheetTests {

        @Test
        @DisplayName("should return 200 OK with registration analysis DTO on sheet sync")
        void shouldSyncRegistrationsSheet() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(50L)
                    .googleSheetUrl("https://docs.google.com/spreadsheets/d/123")
                    .build();

            when(registrationService.syncRegistrationsSheet(1L)).thenReturn(analysis);

            mockMvc.perform(post("/api/exclusive-forms/{id}/registrations/sheet", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(50)))
                    .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/123")));

            verify(registrationService).syncRegistrationsSheet(1L);
        }
    }
}
