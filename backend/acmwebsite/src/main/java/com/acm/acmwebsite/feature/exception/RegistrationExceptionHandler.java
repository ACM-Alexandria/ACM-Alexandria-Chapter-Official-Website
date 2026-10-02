package com.acm.acmwebsite.feature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RegistrationExceptionHandler {

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRegistration(DuplicateRegistrationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EmailsLockedException.class)
    public ResponseEntity<Map<String, String>> handleEmailsLocked(EmailsLockedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // "code" lets the frontend recognise this case and offer to send again with force=true
    @ExceptionHandler(AnnouncementAlreadySentException.class)
    public ResponseEntity<Map<String, String>> handleAnnouncementAlreadySent(AnnouncementAlreadySentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("code", "ANNOUNCEMENT_ALREADY_SENT");
        response.put("sentAt", ex.getSentAt() != null ? ex.getSentAt().toString() : "");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AnnouncementNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleAnnouncementNotAllowed(AnnouncementNotAllowedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleProfileIncomplete(ProfileIncompleteException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingRequiredAnswerException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequiredAnswer(MissingRequiredAnswerException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(GoogleSheetsException.class)
    public ResponseEntity<Map<String, String>> handleGoogleSheetsException(GoogleSheetsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
