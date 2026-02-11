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
        dto.setEventTime(program.getEventTime());
        dto.setGoogleFormUrl(program.getGoogleFormUrl());
        dto.setImageUrl(program.getImageUrl());
        dto.setName(program.getName());
        return dto;
    }
    public Program toProgram(ProgramDto programDto) {
        Program program = new Program();
        program.setId(programDto.getId());
        program.setDescription(programDto.getDescription());
        program.setEventTime(programDto.getEventTime());
        program.setGoogleFormUrl(programDto.getGoogleFormUrl());
        program.setImageUrl(programDto.getImageUrl());
        program.setName(programDto.getName());
        return program;
    }
}
