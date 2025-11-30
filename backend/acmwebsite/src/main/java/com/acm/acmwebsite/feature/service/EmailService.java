package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.subscriptionStatus;
import com.acm.acmwebsite.feature.repository.SubscriptionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {
    private final SubscriptionRepository subscriptionRepository;
    private final JavaMailSender mailSender;
    @Autowired
    public EmailService(SubscriptionRepository subscriptionRepository, JavaMailSender mailSender) {
        this.subscriptionRepository = subscriptionRepository;
        this.mailSender = mailSender;
    }


    @Async
    public void sendEmail(String to, String subject, String text) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    public void notifyAll(String topic,String text) {
        var subscriptions=subscriptionRepository.getSubscriptionsByTopic(topic);
        for(Subscription subscription:subscriptions){
            if(subscription.getStatus()== subscriptionStatus.ACTIVE)
            {
               sendEmail(subscription.getEmail(),"saba70","call is opened now for new members yaboya !");
            }
        }
    }

    public void sendConfirmationEmail(String topic,String email,String text) {
        if(subscriptionRepository.existsByEmailAndTopic(email,topic))
        {
            return;
        }
        var subscription = new Subscription(email,topic);
        sendEmail(email,"subject","confirmation");
        subscription.setStatus(subscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
    }
}
