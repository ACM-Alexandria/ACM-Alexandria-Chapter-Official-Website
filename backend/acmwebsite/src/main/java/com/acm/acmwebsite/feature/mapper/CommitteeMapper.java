package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommitteeMapper {

    CommitteeDto toDto(Committee committee);

    @Mapping(target = "id", ignore = true)
    Committee toEntity(CommitteeDto dto);

    CommitteeBoardDto toBoardDto(CommitteeBoard board);

    @Mapping(target = "committee", ignore = true)
    CommitteeBoard toBoardEntity(CommitteeBoardDto dto);
}