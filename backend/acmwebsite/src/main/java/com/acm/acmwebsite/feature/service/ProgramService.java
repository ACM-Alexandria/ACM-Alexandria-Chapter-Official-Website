package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.mapper.ProgramMapper;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;
    private final ProgramMapper programMapper;
    public ProgramService(ProgramRepository programRepository, ProgramMapper programMapper) {
        this.programRepository = programRepository;
        this.programMapper = programMapper;
    }
    public List<ProgramDto> getAll(){
        return programRepository.findAll().stream().map(programMapper::toProgramDto).toList();
    }
    public Optional<ProgramDto> getById(long id) {
        return programRepository.findById(id).map(programMapper::toProgramDto);
    }
    public void deleteProgram(long id) {
        programRepository.deleteById(id);
    }
    public ProgramDto update(Long id, ProgramDto updatedProgram) {
        Program program = programRepository.findById(id).orElseThrow(() -> new RuntimeException("Program not found!"));
        program.setName(updatedProgram.getName());
        program.setDescription(updatedProgram.getDescription());
        program.setImageUrl(updatedProgram.getImageUrl());
        program.setEventTime(updatedProgram.getEventTime());
        program.setImageUrl(updatedProgram.getImageUrl());
        return programMapper.toProgramDto(programRepository.save(program));
    }
    public List<ProgramDto> searchByName(String name) {
        return programRepository.findByName(name).stream().map(programMapper::toProgramDto).toList();
    }
    public ProgramDto create(ProgramDto programDto) {
        Program program = programMapper.toProgram(programDto);
        return programMapper.toProgramDto(programRepository.save(program));
    }

}
