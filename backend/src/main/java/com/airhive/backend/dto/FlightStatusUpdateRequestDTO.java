package com.airhive.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class FlightStatusUpdateRequestDTO {

    @NotBlank
    private String status;

    private String reason;

    public FlightStatusUpdateRequestDTO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
