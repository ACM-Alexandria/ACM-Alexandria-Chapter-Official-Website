package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailResultDto;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminEmailService {

    private static final int BCC_BATCH_SIZE = 50;
    private static final PolicyFactory EMAIL_BODY_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "blockquote", "br", "div", "em", "h1", "h2", "h3", "li", "ol", "p", "span", "strong",
                    "u", "ul")
            .allowAttributes("href")
            .onElements("a")
            .allowUrlProtocols("http", "https", "mailto")
            .allowStyling()
            .toFactory();

    private final MemberTargetingService memberTargetingService;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SystemSettingsService systemSettingsService;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Transactional(readOnly = true)
    public CustomEmailPreviewDto previewCustomEmail(CustomEmailPreviewRequestDto request) {
        Set<Role> roles = MemberTargetingService.toRoleSet(request.getRoles());
        Set<String> selectedEmails = normalizeEmails(request.getSelectedUserEmails());
        if (roles.isEmpty() && selectedEmails.isEmpty()) {
            throw new IllegalArgumentException("Select at least one role or recipient.");
        }

        List<MemberTargetingService.TargetedMember> filteredMembers = roles.isEmpty()
                ? List.of()
                : memberTargetingService.findMembers(request.getRoles(), request.getCommitteeIds(),
                        request.getClubIds());

        Map<String, String> filteredRecipients = new LinkedHashMap<>();
        for (MemberTargetingService.TargetedMember member : filteredMembers) {
            String email = normalizeEmail(member.user().getEmail());
            if (!email.isEmpty() && !selectedEmails.contains(email)) {
                filteredRecipients.putIfAbsent(email, roleLabel(member.user().getRole()));
            }
        }

        Map<String, Long> recipientCounts = new LinkedHashMap<>();
        filteredRecipients.values().forEach(label -> recipientCounts.merge(label, 1L, Long::sum));
        if (!selectedEmails.isEmpty()) {
            recipientCounts.put("Selected recipients", (long) selectedEmails.size());
        }

        return CustomEmailPreviewDto.builder()
                .totalRecipients(filteredRecipients.size() + selectedEmails.size())
                .recipientCounts(recipientCounts)
                .emailHtml(renderCustomEmail(request.getSubject(), request.getBody()))
                .build();
    }

    public CustomEmailResultDto sendCustomEmail(CustomEmailRequestDto request) {
        systemSettingsService.assertEmailsEnabled();
        List<String> recipients = new ArrayList<>();
        if (!MemberTargetingService.toRoleSet(request.getRoles()).isEmpty()) {
            recipients.addAll(memberTargetingService.findRecipientEmails(
                    request.getRoles(), request.getCommitteeIds(), request.getClubIds()));
        }
        if (request.getSelectedUserEmails() != null) {
            recipients.addAll(request.getSelectedUserEmails());
        }

        List<String> finalRecipients = recipients.stream()
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        if (finalRecipients.isEmpty()) {
            throw new IllegalArgumentException("No accounts match these filters.");
        }

        String emailHtml = renderCustomEmail(request.getSubject(), request.getBody());
        for (int start = 0; start < finalRecipients.size(); start += BCC_BATCH_SIZE) {
            sendBatch(request,finalRecipients.subList(start, Math.min(start + BCC_BATCH_SIZE, finalRecipients.size())), emailHtml);
        }

        return CustomEmailResultDto.builder()
                .message("Email sent to " + finalRecipients.size() + " recipient(s).")
                .totalRecipients(finalRecipients.size())
                .build();
    }

    private static Set<String> normalizeEmails(List<String> emails) {
        if (emails == null) {
            return Set.of();
        }
        return emails.stream()
                .map(AdminEmailService::normalizeEmail)
                .filter(email -> !email.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String roleLabel(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> "Super Admin";
            case ACM_HIGH_BOARD -> "High Board";
            case ACM_COMMITTEE_BOARD -> "Committee Board";
            case ACM_CLUB_BOARD -> "Club Board";
            case ACM_MEMBER -> "ACM Member";
            case USER -> "Standard User";
        };
    }

    private String renderCustomEmail(String subject, String body) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("emailTitle", subject);
        context.setVariable("preheaderText", subject);
        context.setVariable("emailSubject", subject);
        context.setVariable("bodyHtml", EMAIL_BODY_POLICY.sanitize(body));
        context.setVariable("showEmailPreferences", false);
        context.setVariable("websiteUrl", "https://alex.hosting.acm.org/");
        return templateEngine.process("mail/custom-email", context);
    }

    private void sendBatch(CustomEmailRequestDto request, List<String> recipients, String emailHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setBcc(recipients.toArray(String[]::new));
            helper.setSubject(request.getSubject().trim());
            helper.setText(emailHtml, true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Unable to compose the email message.", exception);
        }
    }
}