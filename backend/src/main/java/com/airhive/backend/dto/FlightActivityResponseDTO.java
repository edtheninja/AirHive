package com.airhive.backend.dto;

import java.time.LocalDateTime;

public class FlightActivityResponseDTO {

    private Long id;
    private String eventType;
    private String message;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime createdAt;

    public FlightActivityResponseDTO() {
    }

    public FlightActivityResponseDTO(
            Long id,
            String eventType,
            String message,
            String previousStatus,
            String currentStatus,
            LocalDateTime createdAt) {

        this.id = id;
        this.eventType = eventType;
        this.message = message;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}