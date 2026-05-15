package com.acm.acmwebsite.EmailServer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public final class EmailRequest {

    @NotEmpty(message = "must contain at least one valid email address")
    private final List<@Email(message = "each recipient must be a valid email address") String> to;

    @NotBlank(message = "must not be blank")
    private final String subject;

    @NotBlank(message = "must not be blank")
    private final String templateName;

    private final Map<String, Object> variables;

}
