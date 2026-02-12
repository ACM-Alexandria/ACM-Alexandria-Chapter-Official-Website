package com.acm.acmwebsite.service;


import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.repository.MessageRepository;
import com.acm.acmwebsite.feature.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)

public class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void saveMessage_shouldCallRepositorySave() {
        Message msg = new Message();

        messageService.SaveMessage(msg);

        verify(messageRepository).save(msg);
    }
}
