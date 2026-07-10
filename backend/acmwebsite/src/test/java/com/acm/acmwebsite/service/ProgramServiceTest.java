package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.mapper.ProgramMapper;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.service.ProgramService;
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

        when(programMapper.toProgram(input)).thenReturn(entity);
        when(programRepository.save(entity)).thenReturn(saved);
        when(programMapper.toProgramDto(saved)).thenReturn(output);

        ProgramDto result = programService.createProgram(input);

        assertEquals(output.getName(), result.getName());

        verify(programMapper).toProgram(input);
        verify(programRepository).save(entity);
        verify(programMapper).toProgramDto(saved);
    }
}
