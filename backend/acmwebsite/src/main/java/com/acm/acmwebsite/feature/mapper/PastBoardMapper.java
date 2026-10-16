package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.past.*;
import com.acm.acmwebsite.feature.entity.past.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PastBoardMapper {
    @Mapping(target = "userName", source = "name")
    @Mapping(target = "profileImageUrl", source = "imageUrl")
    @Mapping(target = "order", source = "displayOrder")
    PastHighBoardResponse toResponse(PastHighBoard entity);

    @Mapping(target = "id", ignore = true)
    PastHighBoard toEntity(PastHighBoardRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    void update(PastHighBoardRequest request, @MappingTarget PastHighBoard entity);

    PastCommitteeResponse toResponse(PastCommittee entity);

    @Mapping(target = "id", ignore = true)
    PastCommittee toEntity(PastCommitteeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    void update(PastCommitteeRequest request, @MappingTarget PastCommittee entity);

    PastClubResponse toResponse(PastClub entity);

    @Mapping(target = "id", ignore = true)
    PastClub toEntity(PastClubRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    void update(PastClubRequest request, @MappingTarget PastClub entity);

    @Mapping(target = "userName", source = "name")
    @Mapping(target = "profileImageUrl", source = "imageUrl")
    @Mapping(target = "order", source = "displayOrder")
    @Mapping(target = "pastCommitteeId", source = "pastCommittee.id")
    PastCommitteeBoardResponse toResponse(PastCommitteeBoard entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pastCommittee", ignore = true)
    PastCommitteeBoard toEntity(PastCommitteeBoardRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "pastCommittee", ignore = true)
    void update(PastCommitteeBoardRequest request, @MappingTarget PastCommitteeBoard entity);

    @Mapping(target = "userName", source = "name")
    @Mapping(target = "profileImageUrl", source = "imageUrl")
    @Mapping(target = "order", source = "displayOrder")
    @Mapping(target = "pastClubId", source = "pastClub.id")
    PastClubBoardResponse toResponse(PastClubBoard entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pastClub", ignore = true)
    PastClubBoard toEntity(PastClubBoardRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "pastClub", ignore = true)
    void update(PastClubBoardRequest request, @MappingTarget PastClubBoard entity);

}
