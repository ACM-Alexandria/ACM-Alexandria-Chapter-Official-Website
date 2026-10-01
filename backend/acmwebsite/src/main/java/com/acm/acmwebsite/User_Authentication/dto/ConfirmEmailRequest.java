package com.acm.acmwebsite.User_Authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmEmailRequest {
    @NotBlank(message = "Confirmation token is required")
    private String token;
}
