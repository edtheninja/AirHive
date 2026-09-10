package com.airhive.backend.dto;

import java.time.LocalDateTime;

import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;

public record NotificationResponseDTO(
        Long id,
        Long userId,
        NotificationType type,
        String title,
        String message,
        NotificationSeverity severity,
        String relatedEntityType,
        Long relatedEntityId,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt) {
}
