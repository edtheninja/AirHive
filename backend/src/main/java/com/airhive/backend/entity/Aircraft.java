package com.airhive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "aircraft",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_aircraft_registration",
                        columnNames = "registration_number"
                )
        }
)
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", nullable = false, length = 20)
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_type_id", nullable = false)
    private AircraftType aircraftType;

    @Column(nullable = false, length = 30)
    private String status;

    public Aircraft() {
    }

    public Long getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public AircraftType getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(AircraftType aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}