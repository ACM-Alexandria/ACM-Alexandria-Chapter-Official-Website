package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import com.acm.acmwebsite.feature.entity.ProgramRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.ProgramMapper;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.ProgramService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramMapper programMapper;

    @Mock
    private ProgramFormQuestionRepository programFormQuestionRepository;

    @Mock
    private ProgramRegistrationRepository programRegistrationRepository;

    @Mock
    private GoogleSheetsService googleSheetsService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    private ProgramService programService;

    private Program program() {
        Program p = new Program();
        p.setId(1L);
        p.setName("Bootcamp");
        p.setDescription("Desc");
        p.setImageUrl("img");
        p.setStartDate(LocalDateTime.of(2026, 6, 1, 10, 0));
        p.setEndDate(LocalDateTime.of(2026, 6, 8, 18, 0));
        p.setTime("Every Sunday 6:00 PM");
        return p;
    }

    private ProgramDto dto() {
        ProgramDto d = new ProgramDto();
        d.setName("Bootcamp");
        d.setDescription("Desc");
        d.setImageUrl("img");
        d.setStartDate(LocalDateTime.of(2026, 6, 1, 10, 0));
        d.setEndDate(LocalDateTime.of(2026, 6, 8, 18, 0));
        d.setTime("Every Sunday 6:00 PM");
        return d;
    }

    @Nested
    @DisplayName("getAllPrograms Tests")
    class GetAllProgramsTests {

        @Test
        @DisplayName("getAll should map all programs")
        void getAll_shouldMapAllPrograms() {
            List<Program> programs = List.of(program());

            when(programRepository.findAll(any(Sort.class))).thenReturn(programs);
            when(programMapper.toProgramDto(any())).thenReturn(dto());

            List<ProgramDto> result = programService.getAllPrograms();

            assertEquals(1, result.size());
            verify(programRepository).findAll(any(Sort.class));
            verify(programMapper).toProgramDto(programs.getFirst());
        }
    }

    @Nested
    @DisplayName("getProgramsByPage Tests")
    class GetProgramsByPageTests {

        @Test
        @DisplayName("getProgramsByPage should return paged programs")
        void getProgramsByPage_shouldReturnPagedPrograms() {
            Program p = program();
            ProgramDto d = dto();
            Page<Program> page = new PageImpl<>(List.of(p));

            when(programRepository.findAll(any(PageRequest.class))).thenReturn(page);
            when(programMapper.toProgramDto(p)).thenReturn(d);

            Page<ProgramDto> result = programService.getProgramsByPage(0);

            assertEquals(1, result.getContent().size());
            assertEquals("Bootcamp", result.getContent().getFirst().getName());
        }

        @Test
        @DisplayName("getProgramsByPage with negative page should clamp to zero")
        void getProgramsByPage_negativePage_shouldClampToZero() {
            Page<Program> page = new PageImpl<>(List.of());
            when(programRepository.findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0))).thenReturn(page);

            Page<ProgramDto> result = programService.getProgramsByPage(-2);

            assertTrue(result.getContent().isEmpty());
            verify(programRepository).findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0));
        }
    }

    @Nested
    @DisplayName("getProgramById Tests")
    class GetProgramByIdTests {

        @Test
        @DisplayName("getById found")
        void getById_found() {
            Program p = program();
            ProgramDto d = dto();

            when(programRepository.findById(1L)).thenReturn(Optional.of(p));
            when(programMapper.toProgramDto(p)).thenReturn(d);

            Optional<ProgramDto> result = programService.getProgramById(1L);

            assertTrue(result.isPresent());
            assertEquals("Bootcamp", result.get().getName());
        }

        @Test
        @DisplayName("getById not found")
        void getById_notFound() {
            when(programRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<ProgramDto> result = programService.getProgramById(1L);

            assertTrue(result.isEmpty());
            verify(programMapper, never()).toProgramDto(any());
        }
    }

    @Nested
    @DisplayName("deleteProgram Tests")
    class DeleteProgramTests {

        @Test
        @DisplayName("deleteProgram should call repository cascading deletes")
        void deleteProgram_shouldCallRepository() {
            programService.deleteProgram(1L);
            verify(programRegistrationRepository).deleteByProgramId(1L);
            verify(programFormQuestionRepository).deleteByProgramId(1L);
            verify(programRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("updateProgram Tests")
    class UpdateProgramTests {

        @Test
        @DisplayName("update success")
        void update_success() {
            Program existing = program();
            ProgramDto updated = dto();
            Program saved = program();
            ProgramDto mappedResult = dto();

            when(programRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(programRepository.save(existing)).thenReturn(saved);
            when(programMapper.toProgramDto(saved)).thenReturn(mappedResult);

            ProgramDto result = programService.updateProgram(1L, updated);

            assertEquals(updated.getName(), existing.getName());
            verify(programRepository).save(existing);
            verify(programMapper).toProgramDto(saved);
        }

        @Test
        @DisplayName("update not found should throw RuntimeException")
        void update_notFound_shouldThrow() {
            when(programRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> programService.updateProgram(1L, dto()));

            assertEquals("Program not found!", ex.getMessage());
        }

        @Test
        @DisplayName("updateProgram with null name should throw IllegalArgumentException")
        void updateProgram_nullName_shouldThrow() {
            ProgramDto input = dto();
            input.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> programService.updateProgram(1L, input));
            assertEquals("Program name is required", ex.getMessage());
            verify(programRepository, never()).findById(any());
        }

        @Test
        @DisplayName("updateProgram with blank name should throw IllegalArgumentException")
        void updateProgram_blankName_shouldThrow() {
            ProgramDto input = dto();
            input.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> programService.updateProgram(1L, input));
            assertEquals("Program name is required", ex.getMessage());
            verify(programRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("findProgramByName Tests")
    class FindProgramByNameTests {

        @Test
        @DisplayName("searchByName should map results")
        void searchByName_shouldMapResults() {
            when(programRepository.findByName("Boot")).thenReturn(List.of(program()));
            when(programMapper.toProgramDto(any())).thenReturn(dto());

            List<ProgramDto> result = programService.findProgramByName("Boot");

            assertEquals(1, result.size());
            verify(programRepository).findByName("Boot");
            verify(programMapper).toProgramDto(any());
        }
    }

    @Nested
    @DisplayName("createProgram Tests")
    class CreateProgramTests {

        @Test
        @DisplayName("create should map and save with announcement")
        void create_shouldMapAndSave() {
            ProgramDto input = dto();
            Program entity = program();
            Program saved = program();
            ProgramDto output = dto();

            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(programMapper.toProgram(input)).thenReturn(entity);
            when(programRepository.save(entity)).thenReturn(saved);
            when(programRepository.save(saved)).thenReturn(saved);
            when(programMapper.toProgramDto(saved)).thenReturn(output);

            ProgramDto result = programService.createProgram(input);

            assertEquals(output.getName(), result.getName());
            verify(programMapper).toProgram(input);
            verify(programRepository).save(entity);
            verify(subscriptionService).sendNewProgramNotificationToNewsSubscribers(saved);
            assertNotNull(saved.getAnnouncementSentAt());
            verify(programRepository).save(saved);
            verify(programMapper).toProgramDto(saved);
        }

        @Test
        @DisplayName("create announcement fails should still create without sentAt")
        void create_announcementFails_shouldStillCreateWithoutSentAt() {
            ProgramDto input = dto();
            Program entity = program();

            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(programMapper.toProgram(input)).thenReturn(entity);
            when(programRepository.save(entity)).thenReturn(entity);
            when(programMapper.toProgramDto(entity)).thenReturn(dto());
            doThrow(new RuntimeException("db down")).when(subscriptionService).sendNewProgramNotificationToNewsSubscribers(entity);

            programService.createProgram(input);

            assertNull(entity.getAnnouncementSentAt());
            verify(programRepository, times(1)).save(entity);
        }

        @Test
        @DisplayName("create with sendAnnouncement false should skip announcement")
        void create_withSendAnnouncementFalse_shouldSkipAnnouncement() {
            ProgramDto input = dto();
            input.setSendAnnouncement(false);
            Program entity = program();

            when(programMapper.toProgram(input)).thenReturn(entity);
            when(programRepository.save(entity)).thenReturn(entity);
            when(programMapper.toProgramDto(entity)).thenReturn(dto());

            programService.createProgram(input);

            verify(programRepository).save(entity);
            verify(subscriptionService, never()).sendNewProgramNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("create while emails locked should not announce")
        void create_whileEmailsLocked_shouldNotAnnounce() {
            ProgramDto input = dto();
            Program entity = program();

            when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
            when(programMapper.toProgram(input)).thenReturn(entity);
            when(programRepository.save(entity)).thenReturn(entity);
            when(programMapper.toProgramDto(entity)).thenReturn(dto());

            programService.createProgram(input);

            assertNull(entity.getAnnouncementSentAt());
            verify(subscriptionService, never()).sendNewProgramNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("createProgram with null name should throw IllegalArgumentException")
        void createProgram_nullName_shouldThrow() {
            ProgramDto input = dto();
            input.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> programService.createProgram(input));
            assertEquals("Program name is required", ex.getMessage());
            verify(programRepository, never()).save(any());
        }

        @Test
        @DisplayName("createProgram with blank name should throw IllegalArgumentException")
        void createProgram_blankName_shouldThrow() {
            ProgramDto input = dto();
            input.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> programService.createProgram(input));
            assertEquals("Program name is required", ex.getMessage());
            verify(programRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("announceProgram Tests")
    class AnnounceProgramTests {

        @Test
        @DisplayName("announceProgram should notify subscribers on successful claim")
        void announceProgram_shouldNotifySubscribers() {
            Program entity = program();
            when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(programRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

            programService.announceProgram(1L, false);

            verify(systemSettingsService).assertEmailsEnabled();
            verify(subscriptionService).sendNewProgramNotificationToNewsSubscribers(entity);
        }

        @Test
        @DisplayName("announceProgram already sent should refuse without notifying")
        void announceProgram_alreadySent_shouldRefuseWithoutNotifying() {
            when(programRepository.findById(1L)).thenReturn(Optional.of(program()));
            when(programRepository.claimAnnouncement(eq(1L), any())).thenReturn(0);

            assertThrows(AnnouncementAlreadySentException.class, () -> programService.announceProgram(1L, false));

            verify(subscriptionService, never()).sendNewProgramNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("announceProgram program not found should throw ResourceNotFoundException")
        void announceProgram_notFound_shouldThrow() {
            when(programRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> programService.announceProgram(99L, false));
            assertEquals("Program not found with id 99", ex.getMessage());
        }

        @Test
        @DisplayName("announceProgram force resends announcement and updates announcementSentAt")
        void announceProgram_force_shouldResend() {
            Program entity = program();
            when(programRepository.findById(1L)).thenReturn(Optional.of(entity));

            programService.announceProgram(1L, true);

            assertNotNull(entity.getAnnouncementSentAt());
            verify(programRepository).save(entity);
            verify(programRepository, never()).claimAnnouncement(any(), any());
            verify(subscriptionService).sendNewProgramNotificationToNewsSubscribers(entity);
        }
    }

    @Nested
    @DisplayName("toggleRegistration Tests")
    class ToggleRegistrationTests {

        @Test
        @DisplayName("toggleRegistration program not found should throw ResourceNotFoundException")
        void toggleRegistration_notFound_shouldThrow() {
            when(programRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> programService.toggleRegistration(99L, true));
            assertEquals("Program not found with id 99", ex.getMessage());
        }

        @Test
        @DisplayName("toggleRegistration success updates registrationOpen and returns DTO")
        void toggleRegistration_success() {
            Program entity = program();
            entity.setRegistrationOpen(false);
            ProgramDto mapped = dto();
            mapped.setRegistrationOpen(true);

            when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(programRepository.save(entity)).thenReturn(entity);
            when(programMapper.toProgramDto(entity)).thenReturn(mapped);

            ProgramDto result = programService.toggleRegistration(1L, true);

            assertTrue(entity.isRegistrationOpen());
            assertTrue(result.isRegistrationOpen());
            verify(programRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagementTests {

        @Test
        @DisplayName("createQuestion program not found should throw ResourceNotFoundException")
        void createQuestion_programNotFound_shouldThrow() {
            when(programRepository.findById(99L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> programService.createQuestion(99L, req));
        }

        @Test
        @DisplayName("createQuestion success should save and return response DTO")
        void createQuestion_success() {
            Program program = program();
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Why apply?")
                    .questionType("text")
                    .isRequired(true)
                    .options(List.of("   ", "Option1"))
                    .build();

            when(programRepository.findById(1L)).thenReturn(Optional.of(program));
            when(programFormQuestionRepository.save(any(ProgramFormQuestion.class))).thenAnswer(inv -> {
                ProgramFormQuestion q = inv.getArgument(0);
                q.setId(25L);
                return q;
            });

            FormQuestionResponseDto result = programService.createQuestion(1L, req);

            assertNotNull(result);
            assertEquals(25L, result.getId());
            assertEquals("Why apply?", result.getQuestionText());
            assertEquals("TEXT", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Option1"), result.getOptions());
        }

        @Test
        @DisplayName("updateQuestion question not found should throw ResourceNotFoundException")
        void updateQuestion_notFound_shouldThrow() {
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> programService.updateQuestion(1L, 100L, req));
        }

        @Test
        @DisplayName("updateQuestion program mismatch should throw ResourceNotFoundException")
        void updateQuestion_programMismatch_shouldThrow() {
            Program otherProgram = program();
            otherProgram.setId(999L);
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(100L).program(otherProgram).build();

            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> programService.updateQuestion(1L, 100L, req));
        }

        @Test
        @DisplayName("updateQuestion null program should throw ResourceNotFoundException")
        void updateQuestion_nullProgram_shouldThrow() {
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(100L).program(null).build();
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> programService.updateQuestion(1L, 100L, req));
        }

        @Test
        @DisplayName("updateQuestion success should update fields and return response DTO")
        void updateQuestion_success() {
            Program program = program();
            ProgramFormQuestion q = ProgramFormQuestion.builder()
                    .id(100L)
                    .program(program)
                    .questionText("Old text")
                    .questionType(QuestionType.TEXT)
                    .isRequired(false)
                    .build();

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("New text")
                    .questionType("multiple_choice")
                    .isRequired(true)
                    .options(List.of("OptA", "OptB"))
                    .build();

            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));
            when(programFormQuestionRepository.save(q)).thenReturn(q);

            FormQuestionResponseDto result = programService.updateQuestion(1L, 100L, req);

            assertEquals("New text", result.getQuestionText());
            assertEquals("MULTIPLE_CHOICE", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("OptA", "OptB"), result.getOptions());
        }

        @Test
        @DisplayName("deleteQuestion question not found should throw ResourceNotFoundException")
        void deleteQuestion_notFound_shouldThrow() {
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> programService.deleteQuestion(1L, 100L));
            verify(programFormQuestionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteQuestion program mismatch should throw ResourceNotFoundException")
        void deleteQuestion_programMismatch_shouldThrow() {
            Program other = program();
            other.setId(999L);
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(100L).program(other).build();
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));

            assertThrows(ResourceNotFoundException.class, () -> programService.deleteQuestion(1L, 100L));
            verify(programFormQuestionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteQuestion null program should throw ResourceNotFoundException")
        void deleteQuestion_nullProgram_shouldThrow() {
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(100L).program(null).build();
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));

            assertThrows(ResourceNotFoundException.class, () -> programService.deleteQuestion(1L, 100L));
            verify(programFormQuestionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteQuestion success should delete entity")
        void deleteQuestion_success() {
            Program program = program();
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(100L).program(program).build();
            when(programFormQuestionRepository.findById(100L)).thenReturn(Optional.of(q));

            programService.deleteQuestion(1L, 100L);

            verify(programFormQuestionRepository).delete(q);
        }
    }

    @Nested
    @DisplayName("Registration Analytics & Google Sheets Sync Tests")
    class RegistrationAnalysisTests {

        @Test
        @DisplayName("getRegistrationAnalysis not found should throw ResourceNotFoundException")
        void getRegistrationAnalysis_notFound_shouldThrow() {
            when(programRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> programService.getRegistrationAnalysis(99L));
        }

        @Test
        @DisplayName("getRegistrationAnalysis should compute statistics correctly across diverse registrations")
        void getRegistrationAnalysis_success() {
            Program p = program();
            p.setGoogleSheetUrl("https://docs.google.com/prog-sheet");
            LocalDateTime sheetTime = LocalDateTime.of(2026, 9, 21, 14, 0);
            p.setSheetLastUpdatedAt(sheetTime);

            User userAlexCsed = new User();
            userAlexCsed.setIsAlexEngStudent(true);
            userAlexCsed.setDepartment(Department.CSED);
            userAlexCsed.setBatch("2026");

            User userNonAlexCce = new User();
            userNonAlexCce.setIsAlexEngStudent(false);
            userNonAlexCce.setDepartment(Department.CCE);
            userNonAlexCce.setBatch("2027");

            User userNullDeptAndBatch = new User();
            userNullDeptAndBatch.setIsAlexEngStudent(null);
            userNullDeptAndBatch.setDepartment(null);
            userNullDeptAndBatch.setBatch(null);

            User userBlankBatch = new User();
            userBlankBatch.setIsAlexEngStudent(false);
            userBlankBatch.setDepartment(Department.OTHER);
            userBlankBatch.setBatch("   ");

            ProgramRegistration reg1 = ProgramRegistration.builder().id(1L).user(userAlexCsed).program(p).build();
            ProgramRegistration reg2 = ProgramRegistration.builder().id(2L).user(userNonAlexCce).program(p).build();
            ProgramRegistration reg3 = ProgramRegistration.builder().id(3L).user(userNullDeptAndBatch).program(p).build();
            ProgramRegistration reg4 = ProgramRegistration.builder().id(4L).user(userBlankBatch).program(p).build();
            ProgramRegistration reg5 = ProgramRegistration.builder().id(5L).user(null).program(p).build();

            when(programRepository.findById(1L)).thenReturn(Optional.of(p));
            when(programRegistrationRepository.findByProgramId(1L))
                    .thenReturn(List.of(reg1, reg2, reg3, reg4, reg5));

            RegistrationAnalysisDto analysis = programService.getRegistrationAnalysis(1L);

            assertEquals(5, analysis.getTotalRegistrations());
            assertEquals(1L, analysis.getAlexUniStudentCount());
            assertEquals(4L, analysis.getNonAlexUniStudentCount());

            assertEquals(1L, analysis.getDepartmentCounts().get("CSED"));
            assertEquals(1L, analysis.getDepartmentCounts().get("CCE"));
            assertEquals(1L, analysis.getDepartmentCounts().get("OTHER"));
            assertEquals(1L, analysis.getDepartmentCounts().get("N/A"));

            assertEquals(1L, analysis.getBatchCounts().get("2026"));
            assertEquals(1L, analysis.getBatchCounts().get("2027"));
            assertEquals(2L, analysis.getBatchCounts().get("N/A"));

            assertEquals("https://docs.google.com/prog-sheet", analysis.getGoogleSheetUrl());
            assertEquals(sheetTime, analysis.getSheetLastUpdatedAt());
        }

        @Test
        @DisplayName("syncRegistrationsSheet not found should throw ResourceNotFoundException")
        void syncRegistrationsSheet_notFound_shouldThrow() {
            when(programRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> programService.syncRegistrationsSheet(99L));
        }

        @Test
        @DisplayName("syncRegistrationsSheet should invoke GoogleSheetsService and update sheet metadata")
        void syncRegistrationsSheet_success() {
            Program p = program();
            p.setGoogleSheetUrl("https://docs.google.com/old-prog-url");

            ProgramRegistration reg = ProgramRegistration.builder().id(1L).program(p).build();
            ProgramFormQuestion q = ProgramFormQuestion.builder().id(10L).program(p).questionText("Question?").build();

            when(programRepository.findById(1L)).thenReturn(Optional.of(p));
            when(programRegistrationRepository.findByProgramId(1L)).thenReturn(List.of(reg));
            when(programFormQuestionRepository.findByProgramId(1L)).thenReturn(List.of(q));
            when(googleSheetsService.syncRegistrationData(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn("https://docs.google.com/new-prog-url");
            when(programRepository.save(any(Program.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RegistrationAnalysisDto result = programService.syncRegistrationsSheet(1L);

            assertNotNull(result);
            assertEquals("https://docs.google.com/new-prog-url", p.getGoogleSheetUrl());
            assertNotNull(p.getSheetLastUpdatedAt());
            verify(programRepository).save(p);
            verify(googleSheetsService).syncRegistrationData(
                    eq("ACM Alexandria - Program: Bootcamp - Registrations"),
                    any(),
                    eq("https://docs.google.com/old-prog-url"),
                    eq(Arrays.asList("Program Name:", "Bootcamp", "Start/End Date:", "2026-06-01T10:00 - 2026-06-08T18:00", "Schedule Time:", "Every Sunday 6:00 PM")),
                    eq(List.of(reg)),
                    eq(List.of(q)),
                    any(), any(), any(), any(), any(), any()
            );

            // Also test branch where startDate, endDate, and time are null (prefix header has "N/A - N/A" and "N/A")
            p.setStartDate(null);
            p.setEndDate(null);
            p.setTime(null);
            programService.syncRegistrationsSheet(1L);
            verify(googleSheetsService).syncRegistrationData(
                    eq("ACM Alexandria - Program: Bootcamp - Registrations"),
                    any(),
                    eq("https://docs.google.com/new-prog-url"),
                    eq(Arrays.asList("Program Name:", "Bootcamp", "Start/End Date:", "N/A - N/A", "Schedule Time:", "N/A")),
                    eq(List.of(reg)),
                    eq(List.of(q)),
                    any(), any(), any(), any(), any(), any()
            );
        }
    }
}
