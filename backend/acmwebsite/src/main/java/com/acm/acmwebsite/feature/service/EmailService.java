package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

    }

    @Async
    public void sendEmail(String to, Message message) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(to);
            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), false);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

}
