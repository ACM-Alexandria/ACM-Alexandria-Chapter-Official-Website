package com.acm.acmwebsite.EmailServer.service.impl;

import com.acm.acmwebsite.EmailServer.dto.EmailRequest;
import com.acm.acmwebsite.EmailServer.service.EmailComposer;
import com.acm.acmwebsite.EmailServer.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mail.provider", havingValue = "smtp")
public class SmtpMailServiceImpl implements EmailService {
  @Value("${spring.mail.username}")
  String senderMail;

  private final EmailComposer emailComposer;
  private final JavaMailSender mailSender;
  private static final Logger logger = LoggerFactory.getLogger(SmtpMailServiceImpl.class);

  public SmtpMailServiceImpl(JavaMailSender javaMailSender, EmailComposer emailComposer) {
    this.mailSender = javaMailSender;
    this.emailComposer = emailComposer;
  }

  @Async("mailExecutor")
  @Override
  public void send(@Valid EmailRequest emailRequest) {
    String composedHtml = emailComposer.buildHtmlBody(emailRequest);
    for (String recipent : emailRequest.getTo()) {
      try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(recipent);
        helper.setFrom(senderMail);
        helper.setSubject(emailRequest.getSubject());
        helper.setText(composedHtml, true);
        mailSender.send(message);
      } catch (MessagingException ex) {
        logger.error("Failed to send email to recipient: {}", recipent, ex);
      }
    }
  }
}
