package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.repository.EmailRepository;
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
    private final EmailRepository emailRepository;
    @Autowired
    public EmailService(JavaMailSender mailSender,EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;

    }

    @Async
    public void sendEmail(Email to, Message message) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(to.getEmail());
            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), false);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
    public Email saveEmail(Email email) {
        emailRepository.save(email);
        return email;
    }
    public boolean isEmailExist(Email email) {
      return   emailRepository.existsEmailByEmail(email.getEmail());
    }
    public Email getEmailById(long id) {
        return emailRepository.getEmailByEmailId(id);
    }

    public Email getObjectByEmail(String email) {
       return emailRepository.getEmailByEmail(email);
    }

}
