package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.HRFeedbackRequest;
import com.acm.acmwebsite.feature.dto.HRFeedbackResponse;
import com.acm.acmwebsite.feature.entity.HRFeedback;
import com.acm.acmwebsite.feature.repository.HRFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HRFeedbackService {

    private final HRFeedbackRepository hrFeedbackRepository;
    private final UserRepository userRepository;

    public HRFeedbackService(HRFeedbackRepository hrFeedbackRepository, UserRepository userRepository) {
        this.hrFeedbackRepository = hrFeedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public HRFeedbackResponse saveFeedback(HRFeedbackRequest request, String userEmail) {
        HRFeedback feedback = new HRFeedback();
        feedback.setType(request.getType());
        feedback.setContent(request.getContent());
        feedback.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : true);
        feedback.setCreatedAt(LocalDateTime.now());

        if (!feedback.getIsAnonymous() && userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                feedback.setReporterId(user.getId());
                feedback.setReporter(user);
            }
        }

        HRFeedback saved = hrFeedbackRepository.save(feedback);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HRFeedbackResponse> getAllFeedback() {
        return hrFeedbackRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HRFeedbackResponse mapToResponse(HRFeedback feedback) {
        HRFeedbackResponse response = new HRFeedbackResponse();
        response.setId(feedback.getId());
        response.setType(feedback.getType());
        response.setContent(feedback.getContent());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setIsAnonymous(feedback.getIsAnonymous());

        if (!feedback.getIsAnonymous() && feedback.getReporter() != null) {
            response.setReporterName(feedback.getReporter().getName());
            response.setReporterEmail(feedback.getReporter().getEmail());
        }

        return response;
    }
}
