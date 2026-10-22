package com.acm.acmwebsite.EmailServer.service;

import com.acm.acmwebsite.EmailServer.dto.EmailRequest;
import com.acm.acmwebsite.EmailServer.service.impl.SmtpMailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmtpMailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailComposer emailComposer;

    @InjectMocks
    private SmtpMailServiceImpl smtpMailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(smtpMailService, "senderMail", "sender@acm.org");
    }

    @Test
    @DisplayName("Should compose and send email to all recipients")
    void shouldSendEmailToAllRecipients() {
        EmailRequest request = new EmailRequest(
                List.of("alice@example.com", "bob@example.com"),
                "Welcome to ACM",
                "welcome-template",
                Map.of("user", "Alice")
        );

        when(emailComposer.buildHtmlBody(request)).thenReturn("<html>Welcome!</html>");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        smtpMailService.send(request);

        verify(emailComposer).buildHtmlBody(request);
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should catch MessagingException and log error without throwing")
    void shouldCatchMessagingExceptionGracefully() throws Exception {
        EmailRequest request = new EmailRequest(
                List.of("error@example.com"),
                "Error Test",
                "error-template",
                Map.of()
        );

        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(emailComposer.buildHtmlBody(request)).thenReturn("<html>Error</html>");
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new MessagingException("Invalid address"))
                .when(mockMimeMessage).setRecipient(any(), any());

        assertThatCode(() -> smtpMailService.send(request))
                .doesNotThrowAnyException();

        verify(mailSender, never()).send(mockMimeMessage);
    }
}
