package com.airhive.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AircraftTypeRequestDTO {
    @NotBlank
    private String typeCode;

    @NotBlank
    private String manufacturer;

    @NotBlank
    private String model;

    @NotNull
    @Positive
    private Integer passengerCapacity;

    @NotNull
    @Positive
    private Integer crewCapacity;
   

    public AircraftTypeRequestDTO() {
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getPassengerCapacity() {
        return passengerCapacity;
    }

    public void setPassengerCapacity(Integer passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    public Integer getCrewCapacity() {
        return crewCapacity;
    }

    public void setCrewCapacity(Integer crewCapacity) {
        this.crewCapacity = crewCapacity;
    }
}