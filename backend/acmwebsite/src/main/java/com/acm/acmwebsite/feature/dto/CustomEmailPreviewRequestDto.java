package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CustomEmailPreviewRequestDto {
    private List<Role> roles = new ArrayList<>();
    private List<Long> committeeIds = new ArrayList<>();
    private List<Long> clubIds = new ArrayList<>();
    private List<@Email(message = "Each selected recipient must be a valid email address") String> selectedUserEmails = new ArrayList<>();
}