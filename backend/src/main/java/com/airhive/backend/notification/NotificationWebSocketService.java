package com.airhive.backend.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.NotificationResponseDTO;

@Service
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishNotification(NotificationResponseDTO notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + notification.userId(),
                notification
        );
    }
}