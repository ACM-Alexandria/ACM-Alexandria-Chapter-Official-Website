package com.acm.acmwebsite.service;

import com.acm.acmwebsite.core.service.impl.GmailEmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private GmailEmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "from@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("Should send password reset email successfully")
    void shouldSendPasswordResetEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/password-reset"), any(Context.class)))
                .thenReturn("<html>reset</html>");

        emailService.sendPasswordResetEmail("user@example.com", "dummy-token");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send registration confirmation email successfully")
    void shouldSendRegistrationConfirmationEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/registration-confirmation"), any(Context.class)))
                .thenReturn("<html>registered</html>");

        emailService.sendRegistrationConfirmationEmail("user@example.com", "Kickoff Event", "John Doe");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void shouldSendWelcomeEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/welcome-email"), any(Context.class)))
                .thenReturn("<html>welcome</html>");

        emailService.sendWelcomeEmail("user@example.com", "John Doe");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send subscription confirmation email successfully")
    void shouldSendSubscriptionConfirmationEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/subscription-confirmation"), any(Context.class)))
                .thenReturn("<html>subscribed</html>");

        emailService.sendSubscriptionConfirmationEmail("user@example.com", "Subscribed", "Body info");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send new event announcement email successfully")
    void shouldSendNewEventAnnouncementEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/new-event"), any(Context.class)))
                .thenReturn("<html>event</html>");

        emailService.sendNewEventAnnouncementEmail("user@example.com", "Hackathon", "10:00 AM", "Hall A");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send new club announcement email successfully")
    void shouldSendNewClubAnnouncementEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/new-club"), any(Context.class)))
                .thenReturn("<html>club</html>");

        emailService.sendNewClubAnnouncementEmail("user@example.com", "CP Club", "Competitive Programming");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should send committee call email successfully")
    void shouldSendCommitteeCallEmail() {
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq("mail/committee-call"), any(Context.class)))
                .thenReturn("<html>committee</html>");

        emailService.sendCommitteeCallEmail("user@example.com", "Call for PR", "Apply now!");

        verify(mailSender, times(1)).send(mockMimeMessage);
    }
}