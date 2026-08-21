package com.airhive.backend.dto;

public class AircraftResponseDTO {

    private Long id;

    private String registrationNumber;

    private Long aircraftTypeId;

    private String aircraftTypeCode;

    private String status;

    public AircraftResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAircraftTypeCode() {
        return aircraftTypeCode;
    }

    public void setAircraftTypeCode(String aircraftTypeCode) {
        this.aircraftTypeCode = aircraftTypeCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
