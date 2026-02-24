package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import org.springframework.stereotype.Component;

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

        return committee;
    }
}