package com.airhive.backend.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.airhive.backend.event.NotificationCreatedEvent;
import com.airhive.backend.notification.NotificationWebSocketService;

@Component
public class NotificationWebSocketEventListener {

    private final NotificationWebSocketService notificationWebSocketService;

    public NotificationWebSocketEventListener(
            NotificationWebSocketService notificationWebSocketService) {

        this.notificationWebSocketService = notificationWebSocketService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(
            NotificationCreatedEvent event) {

        notificationWebSocketService.publishNotification(
                event.notification());
    }
}