package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.entity.EmailConfirmationRequest;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.EmailAlreadyConfirmedException;
import com.acm.acmwebsite.User_Authentication.exception.RateLimitException;
import com.acm.acmwebsite.User_Authentication.repository.EmailConfirmationRequestRepository;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailConfirmationService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationServiceImpl implements EmailConfirmationService {
    private final UserRepository userRepository;
    private final EmailConfirmationRequestRepository rateLimitRepository;
    private  final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Override
    @Transactional
    public void sendConfirmationEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        if (Boolean.TRUE.equals(user.getEmailConfirmed())) {
            throw new EmailAlreadyConfirmedException("Email already confirmed.");
        }

        long minuteCount = rateLimitRepository.countByEmailAndRequestedAtAfter(email, LocalDateTime.now().minusMinutes(1));

        if (minuteCount >= 1) {
            throw new RateLimitException("Confirmation email already sent. Please wait before requesting again.");
        }

        String token = jwtUtil.generateEmailConfirmationToken(email);
        String encodedToken = jwtUtil.encodeTokenForUrl(token);
        rateLimitRepository.save(
                EmailConfirmationRequest.builder()
                        .email(email)
                        .requestedAt(LocalDateTime.now())
                        .build()
        );

        emailService.sendEmailConfirmationEmail(email, encodedToken, user.getName());

        log.info("Confirmation email sent to {}", email);
    }

    @Override
    @Transactional
    public void confirmEmail(String encodedToken) {
        String token = jwtUtil.decodeTokenFromUrl(encodedToken);
        String email = jwtUtil.validateEmailConfirmationToken(token);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        if (Boolean.TRUE.equals(user.getEmailConfirmed())) {
            throw new EmailAlreadyConfirmedException("Email already confirmed.");
        }
        user.setEmailConfirmed(true);
        userRepository.save(user);
        log.info("Email confirmed for {}", email);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), "ACM Member");
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", user.getEmail(), e);
        }
    }
}
