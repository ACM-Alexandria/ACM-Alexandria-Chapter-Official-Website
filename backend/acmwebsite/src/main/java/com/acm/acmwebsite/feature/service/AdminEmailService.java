package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailResultDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEmailService {

    private static final int BCC_BATCH_SIZE = 50;

    private final MemberTargetingService memberTargetingService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public CustomEmailResultDto sendCustomEmail(CustomEmailRequestDto request) {
        List<String> recipients = memberTargetingService.findRecipientEmails(
                request.getRoles(), request.getCommitteeIds(), request.getClubIds());
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No accounts match these filters.");
        }

        for (int start = 0; start < recipients.size(); start += BCC_BATCH_SIZE) {
            sendBatch(request, recipients.subList(start, Math.min(start + BCC_BATCH_SIZE, recipients.size())));
        }

        return CustomEmailResultDto.builder()
                .message("Email sent to " + recipients.size() + " recipient(s).")
                .totalRecipients(recipients.size())
                .build();
    }

    private void sendBatch(CustomEmailRequestDto request, List<String> recipients) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setBcc(recipients.toArray(String[]::new));
            helper.setSubject(request.getSubject().trim());
            helper.setText(request.getBody(), true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Unable to compose the email message.", exception);
        }
    }
}