package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    public void SaveMessage(Message message){
        messageRepository.save(message);
    }
}
