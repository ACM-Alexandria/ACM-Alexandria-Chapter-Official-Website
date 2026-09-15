package com.acm.acmwebsite.User_Authentication.dto;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssignmentDto {
    private Role targetRole;
    private String boardRole;
    private Integer boardOrder;
    private Long committeeId;
    private Long clubId;
}
