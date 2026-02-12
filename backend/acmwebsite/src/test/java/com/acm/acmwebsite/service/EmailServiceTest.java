package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.repository.EmailRepository;
import com.acm.acmwebsite.feature.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailRepository emailRepository;

    @InjectMocks
    private EmailService emailService;

    // --- sendEmail Tests ---

    @Test
    @DisplayName("Should send email successfully")
    void shouldSendEmailSuccessfully() {
        // Arrange
        Email to = new Email("member@acm.com");
        Message message = new Message("Test Subject", "Test Body");
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        // Required: service calls mailSender.createMimeMessage()
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailService.sendEmail(to, message);

        // Assert
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("Should throw RuntimeException when mail sending fails")
    void shouldThrowExceptionOnMailFailure() {
        // Arrange
        Email to = new Email("fail@acm.com");
        Message message = new Message("Fail", "Fail");

        // Simulating an error during message creation or sending
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.sendEmail(to, message));
    }

    // --- Repository Interaction Tests ---

    @Test
    @DisplayName("Should save email and return the saved object")
    void shouldSaveEmail() {
        // Arrange
        Email email = new Email("new@acm.com");
        when(emailRepository.save(email)).thenReturn(email);

        // Act
        Email result = emailService.saveEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals("new@acm.com", result.getEmail());
        verify(emailRepository, times(1)).save(email);
    }

    @Test
    @DisplayName("Should return true if email address already exists")
    void shouldCheckIfEmailExists() {
        // Arrange
        Email email = new Email("exists@acm.com");
        when(emailRepository.existsEmailByEmail("exists@acm.com")).thenReturn(true);

        // Act
        boolean exists = emailService.isEmailExist(email);

        // Assert
        assertTrue(exists);
        verify(emailRepository).existsEmailByEmail("exists@acm.com");
    }

    @Test
    @DisplayName("Should return email object by its database ID")
    void shouldGetEmailById() {
        // Arrange
        long id = 1L;
        Email expectedEmail = new Email("found@acm.com");
        when(emailRepository.getEmailByEmailId(id)).thenReturn(expectedEmail);

        // Act
        Email result = emailService.getEmailById(id);

        // Assert
        assertNotNull(result);
        assertEquals("found@acm.com", result.getEmail());
    }

    @Test
    @DisplayName("Should return email object by its string address")
    void shouldGetObjectByEmail() {
        // Arrange
        String emailStr = "search@acm.com";
        Email expectedEmail = new Email(emailStr);
        when(emailRepository.getEmailByEmail(emailStr)).thenReturn(expectedEmail);

        // Act
        Email result = emailService.getObjectByEmail(emailStr);

        // Assert
        assertNotNull(result);
        assertEquals(emailStr, result.getEmail());
    }
}