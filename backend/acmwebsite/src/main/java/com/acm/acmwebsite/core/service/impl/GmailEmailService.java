package com.acm.acmwebsite.core.service.impl;

import com.acm.acmwebsite.core.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailEmailService implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    // Inject your email from properties to use as the "From" address
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Async // Optimization: Send email in background so the user doesn't wait
    public void sendPasswordResetEmail(String to, String rawToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("ACM Website - Password Reset Request");

            String link = frontendUrl + "/reset-password?token=" + rawToken;

            Context context = new Context();
            context.setVariable("emailTitle", "Password Reset Request — ACM Alexandria");
            context.setVariable("preheaderText", "Reset your password for your ACM Alexandria Chapter account.");
            context.setVariable("buttonUrl", link);

            String htmlContent = templateEngine.process("mail/password-reset", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendRegistrationConfirmationEmail(String to, String itemName, String userName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("ACM Website - Registration Confirmation: " + itemName);

            Context context = new Context();
            context.setVariable("emailTitle", "Registration Confirmed — ACM Alexandria");
            context.setVariable("preheaderText", "You have successfully registered for " + itemName);
            context.setVariable("userName", userName);
            context.setVariable("itemName", itemName);

            String htmlContent = templateEngine.process("mail/registration-confirmation", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Registration confirmation email sent successfully to {} for item {}", to, itemName);

        } catch (Exception e) {
            log.error("Failed to send registration confirmation email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to ACM Alexandria Student Chapter! 🎉");

            String link = frontendUrl + "/login";

            Context context = new Context();
            context.setVariable("emailTitle", "Welcome to ACM Alexandria — Account Activated");
            context.setVariable("preheaderText", "Welcome to the ACM Alexandria Chapter community!");
            context.setVariable("userName", userName);
            context.setVariable("websiteUrl", link);

            String htmlContent = templateEngine.process("mail/welcome-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Welcome email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendSubscriptionConfirmationEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "Subscription Confirmation — ACM Alexandria");

            Context context = new Context();
            context.setVariable("emailTitle", subject != null ? subject : "Subscription Confirmation — ACM Alexandria");
            context.setVariable("preheaderText", "Your subscription to our mailing list has been confirmed.");
            context.setVariable("messageText", body);

            String htmlContent = templateEngine.process("mail/subscription-confirmation", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Subscription confirmation email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendNewEventAnnouncementEmail(String to, String eventName, String eventTime, String eventLocation) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("New ACM Alexandria Event: " + eventName);

            Context context = new Context();
            context.setVariable("emailTitle", "New ACM Alexandria Event: " + eventName);
            context.setVariable("preheaderText", "A new event has been scheduled. Check out the details!");
            context.setVariable("eventName", eventName);
            context.setVariable("eventTime", eventTime != null ? eventTime : "To be announced");
            context.setVariable("eventLocation", eventLocation != null ? eventLocation : "To be announced");
            context.setVariable("buttonUrl", frontendUrl + "/events");

            String htmlContent = templateEngine.process("mail/new-event", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("New event announcement email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send new event announcement email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendNewClubAnnouncementEmail(String to, String clubName, String description) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("New ACM Alexandria Club: " + clubName);

            Context context = new Context();
            context.setVariable("emailTitle", "New ACM Alexandria Club: " + clubName);
            context.setVariable("preheaderText", "We are thrilled to announce the launch of a new ACM Alexandria student club!");
            context.setVariable("clubName", clubName);
            context.setVariable("description", description != null ? description : "No description available");
            context.setVariable("buttonUrl", frontendUrl + "/clubs");

            String htmlContent = templateEngine.process("mail/new-club", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("New club announcement email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send new club announcement email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendCommitteeCallEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "Committee Announcement");

            Context context = new Context();
            context.setVariable("emailTitle", subject != null ? subject : "Committee Announcement");
            context.setVariable("preheaderText", "We have a new committee call for you!");
            context.setVariable("title", subject != null ? subject : "Committee Call Active");
            context.setVariable("messageText", body);
            context.setVariable("buttonUrl", frontendUrl + "/committees");

            String htmlContent = templateEngine.process("mail/committee-call", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Committee call email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send committee call email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendCommitteeRegistrationConfirmationEmail(String to, String committeeName, String userName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("ACM Website - Committee Application Received: " + committeeName);

            Context context = new Context();
            context.setVariable("emailTitle", "Committee Application Received — ACM Alexandria");
            context.setVariable("preheaderText", "We have received your application for the " + committeeName + " Committee.");
            context.setVariable("userName", userName);
            context.setVariable("committeeName", committeeName);

            String htmlContent = templateEngine.process("mail/committee-registration-confirmation", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Committee registration confirmation email sent successfully to {} for committee {}", to, committeeName);

        } catch (Exception e) {
            log.error("Failed to send committee registration confirmation email to {}", to, e);
        }
    }
}

