package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PastBoardSnapshotMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", source = "season")
    @Mapping(target = "name", source = "source.name")
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "logoUrl", source = "source.logoUrl")
    PastCommittee toPast(Committee source, Integer season);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", source = "season")
    @Mapping(target = "name", source = "source.name")
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "logoUrl", source = "source.imageUrl")
    PastClub toPast(Club source, Integer season);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", source = "season")
    @Mapping(target = "name", source = "board.user.name")
    @Mapping(target = "imageUrl", source = "board.user.profileImageUrl")
    @Mapping(target = "linkedinUrl", source = "board.user.linkedinUrl")
    @Mapping(target = "role", source = "board.role")
    @Mapping(target = "displayOrder", source = "board.order")
    PastHighBoard toPast(HighBoard board, Integer season);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", source = "season")
    @Mapping(target = "name", source = "board.user.name")
    @Mapping(target = "imageUrl", source = "board.user.profileImageUrl")
    @Mapping(target = "linkedinUrl", source = "board.user.linkedinUrl")
    @Mapping(target = "role", source = "board.role")
    @Mapping(target = "displayOrder", source = "board.order")
    @Mapping(target = "pastCommittee", ignore = true)
    PastCommitteeBoard toPast(CommitteeBoard board, Integer season);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", source = "season")
    @Mapping(target = "name", source = "board.user.name")
    @Mapping(target = "imageUrl", source = "board.user.profileImageUrl")
    @Mapping(target = "linkedinUrl", source = "board.user.linkedinUrl")
    @Mapping(target = "role", source = "board.role")
    @Mapping(target = "displayOrder", source = "board.order")
    @Mapping(target = "pastClub", ignore = true)
    PastClubBoard toPast(ClubBoard board, Integer season);
}
