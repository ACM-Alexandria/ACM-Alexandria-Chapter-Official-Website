package com.acm.acmwebsite.core.service.impl;

import com.acm.acmwebsite.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailEmailService implements EmailService {

    private final JavaMailSender javaMailSender;

    // Inject your email from properties to use as the "From" address
    @Value("${spring.mail.username}")
    private String senderEmail;

    // TODO: Change this to your actual Frontend URL (React/Angular/Vue)
    private final String FRONTEND_URL = "http://example.com/reset-password";

    @Override
    @Async // Optimization: Send email in background so the user doesn't wait
    public void sendPasswordResetEmail(String to, String rawToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("ACM Website - Password Reset Request");

            String link = FRONTEND_URL + "?token=" + rawToken;

            String emailBody = """
                Hello,
                
                You have requested to reset your password.
                Click the link below to verify your email and set a new password:
                
                %s
                
                This link is valid for 15 minutes.
                If you did not request this, please ignore this email.
                """.formatted(link);

            message.setText(emailBody);

            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            // In production, you might want to throw a custom exception here
        }
    }

    @Override
    @Async
    public void sendRegistrationConfirmationEmail(String to, String itemName, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("ACM Website - Registration Confirmation: " + itemName);

            String emailBody = """
                Hello %s,
                
                Congratulations! You have successfully registered for: %s.
                
                We are extremely excited to have you participate!
                Should you have any inquiries or need assistance, please don't hesitate to reach out to our team.
                
                Best regards,
                ACM Alexandria Student Chapter
                """.formatted(userName, itemName);

            message.setText(emailBody);

            javaMailSender.send(message);
            log.info("Registration confirmation email sent successfully to {} for item {}", to, itemName);

        } catch (Exception e) {
            log.error("Failed to send registration confirmation email to {}", to, e);
        }
    }
}

