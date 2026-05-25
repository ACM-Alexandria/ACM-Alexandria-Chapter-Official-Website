package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CommitteeMapper {

    CommitteeDto toDto(Committee committee);

    @Mapping(target = "id", ignore = true)
    Committee toEntity(CommitteeDto dto);

    CommitteeBoardMemberDto toBoardDto(CommitteeBoard board);

    @Mapping(target = "committee", ignore = true)
    CommitteeBoard toBoardEntity(CommitteeBoardMemberDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateCommitteeFromDto(CommitteeDto dto, @MappingTarget Committee committee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "committee", ignore = true)
    void updateBoardEntityFromDto(CommitteeBoardMemberDto dto, @MappingTarget CommitteeBoard boardEntity);
}