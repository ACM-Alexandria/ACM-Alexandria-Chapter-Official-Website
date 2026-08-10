package com.acm.acmwebsite.core.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface EmailService {
    void sendPasswordResetEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email, String rawToken, String userName);

    void sendRegistrationConfirmationEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String itemName, String userName);

    void sendWelcomeEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String userName);

    void sendSubscriptionConfirmationEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String subject, String body, String userName);

    void sendNewEventAnnouncementEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String eventName, String eventTime, String eventLocation, String userName);

    void sendNewClubAnnouncementEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String clubName, String description, String userName);

    void sendNewProgramAnnouncementEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String programName, String description, String startDate, String userName);

    void sendCommitteeCallEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String subject, String body, String userName);

    void sendCommitteeRegistrationConfirmationEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String committeeName, String userName);

    void sendGenericFormSubmissionConfirmationEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String to, String formName, String userName);
}
