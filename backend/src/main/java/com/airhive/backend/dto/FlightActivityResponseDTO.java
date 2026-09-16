package com.airhive.backend.dto;

import java.time.LocalDateTime;

public class FlightActivityResponseDTO {

    private Long id;
    private String eventType;
    private String message;
    private String previousStatus;
    private String currentStatus;
    private String reason;
    private LocalDateTime createdAt;

    public FlightActivityResponseDTO() {
    }

    public FlightActivityResponseDTO(
            Long id,
            String eventType,
            String message,
            String previousStatus,
            String currentStatus,
            String reason,
            LocalDateTime createdAt) {

        this.id = id;
        this.eventType = eventType;
        this.message = message;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.reason = reason;
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

    public String getReason() {
        return reason;      
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
