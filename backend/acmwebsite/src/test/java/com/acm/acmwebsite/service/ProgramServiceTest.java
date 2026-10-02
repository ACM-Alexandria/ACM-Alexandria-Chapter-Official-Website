package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.mapper.ProgramMapper;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.service.ProgramService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        p.setStartDate(LocalDateTime.now());
        p.setEndDate(LocalDateTime.now().plusDays(7));
        p.setTime("Every Sunday 6:00 PM");
        return p;
    }

    private ProgramDto dto() {
        ProgramDto d = new ProgramDto();
        d.setName("Bootcamp");
        d.setDescription("Desc");
        d.setImageUrl("img");
        d.setStartDate(LocalDateTime.now());
        d.setEndDate(LocalDateTime.now().plusDays(7));
        d.setTime("Every Sunday 6:00 PM");
        return d;
    }



    @Test
    void getAll_shouldMapAllPrograms() {
        List<Program> programs = List.of(program());

        when(programRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(programs);
        when(programMapper.toProgramDto(any()))
                .thenReturn(dto());

        List<ProgramDto> result = programService.getAllPrograms();

        assertEquals(1, result.size());
        verify(programRepository).findAll(any(org.springframework.data.domain.Sort.class));
        verify(programMapper).toProgramDto(programs.get(0));
    }



    @Test
    void getById_found() {
        Program p = program();
        ProgramDto d = dto();

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(p));
        when(programMapper.toProgramDto(p))
                .thenReturn(d);

        Optional<ProgramDto> result = programService.getProgramById(1L);

        assertTrue(result.isPresent());
        assertEquals("Bootcamp", result.get().getName());
    }

    @Test
    void getById_notFound() {
        when(programRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<ProgramDto> result = programService.getProgramById(1L);

        assertTrue(result.isEmpty());
        verify(programMapper, never()).toProgramDto(any());
    }



    @Test
    void deleteProgram_shouldCallRepository() {
        programService.deleteProgram(1L);
        verify(programRegistrationRepository).deleteByProgramId(1L);
        verify(programFormQuestionRepository).deleteByProgramId(1L);
        verify(programRepository).deleteById(1L);
    }



    @Test
    void update_success() {
        Program existing = program();
        ProgramDto updated = dto();
        Program saved = program();
        ProgramDto mappedResult = dto();

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(programRepository.save(existing))
                .thenReturn(saved);

        when(programMapper.toProgramDto(saved))
                .thenReturn(mappedResult);

        ProgramDto result = programService.updateProgram(1L, updated);

        assertEquals(updated.getName(), existing.getName());
        verify(programRepository).save(existing);
        verify(programMapper).toProgramDto(saved);
    }



    @Test
    void update_notFound_shouldThrow() {
        when(programRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> programService.updateProgram(1L, dto())
        );

        assertEquals("Program not found!", ex.getMessage());
    }

    @Test
    void searchByName_shouldMapResults() {
        when(programRepository.findByName("Boot"))
                .thenReturn(List.of(program()));
        when(programMapper.toProgramDto(any()))
                .thenReturn(dto());

        List<ProgramDto> result = programService.findProgramByName("Boot");

        assertEquals(1, result.size());
        verify(programRepository).findByName("Boot");
        verify(programMapper).toProgramDto(any());
    }

    @Test
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
        // Saved again to record the announcement time once it went out
        assertNotNull(saved.getAnnouncementSentAt());
        verify(programRepository).save(saved);
        verify(programMapper).toProgramDto(saved);
    }

    @Test
    void create_announcementFails_shouldStillCreateWithoutSentAt() {
        ProgramDto input = dto();
        Program entity = program();

        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(programMapper.toProgram(input)).thenReturn(entity);
        when(programRepository.save(entity)).thenReturn(entity);
        when(programMapper.toProgramDto(entity)).thenReturn(dto());
        doThrow(new RuntimeException("db down")).when(subscriptionService).sendNewProgramNotificationToNewsSubscribers(entity);

        // Doesn't throw: the program is created even though the announcement failed
        programService.createProgram(input);

        assertNull(entity.getAnnouncementSentAt());
        verify(programRepository, times(1)).save(entity);
    }

    @Test
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
    void create_whileEmailsLocked_shouldNotAnnounce() {
        ProgramDto input = dto();
        Program entity = program();

        when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
        when(programMapper.toProgram(input)).thenReturn(entity);
        when(programRepository.save(entity)).thenReturn(entity);
        when(programMapper.toProgramDto(entity)).thenReturn(dto());

        programService.createProgram(input);

        // The announcement was never sent, so it must still be sendable later
        assertNull(entity.getAnnouncementSentAt());
        verify(subscriptionService, never()).sendNewProgramNotificationToNewsSubscribers(any());
    }

    @Test
    void announceProgram_shouldNotifySubscribers() {
        Program entity = program();
        when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(programRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

        programService.announceProgram(1L, false);

        verify(subscriptionService).sendNewProgramNotificationToNewsSubscribers(entity);
    }

    @Test
    void announceProgram_alreadySent_shouldRefuseWithoutNotifying() {
        when(programRepository.findById(1L)).thenReturn(Optional.of(program()));
        when(programRepository.claimAnnouncement(eq(1L), any())).thenReturn(0);

        assertThrows(AnnouncementAlreadySentException.class, () -> programService.announceProgram(1L, false));

        verify(subscriptionService, never()).sendNewProgramNotificationToNewsSubscribers(any());
    }
}
