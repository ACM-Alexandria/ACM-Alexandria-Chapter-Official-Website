package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.entity.EmailConfirmationRateLimit;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.EmailConfirmationRateLimitRepository;
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
    private final EmailConfirmationRateLimitRepository rateLimitRepository;
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
            return;
        }

        long hourlyCount = rateLimitRepository.countByEmailAndRequestedAtAfter(email, LocalDateTime.now().minusHours(1));
        long dailyCount  = rateLimitRepository.countByEmailAndRequestedAtAfter(email, LocalDateTime.now().minusDays(1));

        if (hourlyCount >= 1) {
            throw new IllegalStateException("Confirmation email already sent. Please wait before requesting again.");
        }
        if (dailyCount >= 6) {
            throw new IllegalStateException("Daily confirmation email limit reached. Please try again tomorrow.");
        }

        String token = jwtUtil.generateEmailConfirmationToken(email);
        rateLimitRepository.save(
                EmailConfirmationRateLimit.builder()
                        .email(email)
                        .requestedAt(LocalDateTime.now())
                        .build()
        );

        emailService.sendEmailConfirmationEmail(email, token, user.getName());

        log.info("Confirmation email sent to {}", email);
    }

    @Override
    @Transactional
    public void confirmEmail(String token) {
        String email = jwtUtil.validateEmailConfirmationToken(token);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        if (Boolean.TRUE.equals(user.getEmailConfirmed())) {
            return;
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
