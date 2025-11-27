package com.acm.acmwebsite.core.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface EmailService {
    void sendPasswordResetEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email, String rawToken);
}
