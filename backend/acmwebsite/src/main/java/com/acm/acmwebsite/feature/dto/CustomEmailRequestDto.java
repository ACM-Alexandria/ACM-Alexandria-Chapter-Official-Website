package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CustomEmailRequestDto {

    @NotBlank
    @Pattern(regexp = "FILTERED_USERS")
    private String recipientFilter;

    @NotBlank
    @Size(max = 200)
    @Pattern(regexp = "[^\\r\\n]*")
    private String subject;

    @NotBlank
    @Size(max = 100000)
    private String body;

    @NotEmpty
    private List<Role> roles;

    private List<Long> committeeIds = new ArrayList<>();
    private List<Long> clubIds = new ArrayList<>();
}