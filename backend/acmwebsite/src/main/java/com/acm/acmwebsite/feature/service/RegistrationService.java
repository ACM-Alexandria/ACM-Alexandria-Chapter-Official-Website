package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;

import java.util.List;
import java.util.UUID;

public interface RegistrationService {
    List<FormQuestionResponseDto> getQuestions(Long entityId);
    void registerUser(UUID userId, Long entityId, RegistrationRequestDto request);
    boolean isUserRegistered(UUID userId, Long entityId);
}
