package com.airhive.backend.dto;

public class AircraftRequestDTO {

    private String registrationNumber;

    private Long aircraftTypeId;

    private String status;

    public AircraftRequestDTO() {
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Long getAircraftTypeId() {
        return aircraftTypeId;
    }

    public void setAircraftTypeId(Long aircraftTypeId) {
        this.aircraftTypeId = aircraftTypeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}