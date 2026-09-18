package com.airhive.backend.event;

import com.airhive.backend.dto.NotificationResponseDTO;

public record NotificationCreatedEvent(
        NotificationResponseDTO notification) {
}