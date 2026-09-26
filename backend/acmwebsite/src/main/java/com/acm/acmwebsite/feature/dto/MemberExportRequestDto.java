package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExportRequestDto {
    private List<Role> roles;
    private List<Long> committeeIds;
    private List<Long> clubIds;
    private String title;
}
