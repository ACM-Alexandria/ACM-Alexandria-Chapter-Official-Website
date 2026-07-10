package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.entity.Program;
import org.springframework.stereotype.Component;

@Component
public class ProgramMapper {
    public ProgramDto toProgramDto(Program program) {
        ProgramDto dto = new ProgramDto();
        dto.setId(program.getId());
        dto.setDescription(program.getDescription());
        dto.setStartDate(program.getStartDate());
        dto.setEndDate(program.getEndDate());
        dto.setTime(program.getTime());
        dto.setImageUrl(program.getImageUrl());
        dto.setName(program.getName());
        dto.setRegistrationOpen(program.isRegistrationOpen());
        dto.setGoogleSheetUrl(program.getGoogleSheetUrl());
        dto.setSheetLastUpdatedAt(program.getSheetLastUpdatedAt());
        return dto;
    }

    public Program toProgram(ProgramDto programDto) {
        Program program = new Program();
        program.setId(programDto.getId());
        program.setDescription(programDto.getDescription());
        program.setStartDate(programDto.getStartDate());
        program.setEndDate(programDto.getEndDate());
        program.setTime(programDto.getTime());
        program.setImageUrl(programDto.getImageUrl());
        program.setName(programDto.getName());
        program.setRegistrationOpen(programDto.isRegistrationOpen());
        return program;
    }
}
