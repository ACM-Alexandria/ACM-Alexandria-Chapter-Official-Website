package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommitteeMapper {

    public CommitteeDto toDto(Committee committee) {
        if (committee == null) {
            return null;
        }

        CommitteeDto dto = new CommitteeDto();
        dto.setId(committee.getId());
        dto.setName(committee.getName());
        dto.setDescription(committee.getDescription());
        dto.setLogoUrl(committee.getLogoUrl());
        dto.setOpen(committee.isOpen());
        dto.setCallMessage(committee.getCallMessage());
        dto.setApplicationFormLink(committee.getApplicationFormLink());
        dto.setBoardRoles(toBoardRoleDtos(committee.getBoardRoles()));

        return dto;
    }

    public Committee toEntity(CommitteeDto dto) {
        if (dto == null) {
            return null;
        }

        Committee committee = new Committee();
        committee.setName(dto.getName());
        committee.setDescription(dto.getDescription());
        committee.setLogoUrl(dto.getLogoUrl());
        committee.setOpen(dto.isOpen());
        committee.setCallMessage(dto.getCallMessage());
        committee.setApplicationFormLink(dto.getApplicationFormLink());
        committee.setBoardRoles(toBoardRoleEntities(dto.getBoardRoles()));

        return committee;
    }

    private List<CommitteeBoardDto> toBoardRoleDtos(List<CommitteeBoard> boardRoles) {
        if (boardRoles == null) {
            return List.of();
        }
        return boardRoles.stream().map(role -> new CommitteeBoardDto(
                role.getId(),
                role.getName(),
                role.getImageUrl(),
                role.getRole(),
                role.getOrder(),
                role.getLinkedinUrl())).toList();
    }

    private List<CommitteeBoard> toBoardRoleEntities(List<CommitteeBoardDto> boardRoles) {
        if (boardRoles == null) {
            return List.of();
        }
        return boardRoles.stream().map(roleDto -> new CommitteeBoard(
                roleDto.getId(),
                roleDto.getName(),
                roleDto.getImageUrl(),
                roleDto.getRole(),
                roleDto.getOrder(),
                roleDto.getLinkedinUrl())).toList();
    }
}