
package com.airhive.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateBookingRequestDTO {

    @NotNull(message = "Flight ID is required.")
    @Positive(message = "Flight ID must be positive.")
    private Long flightId;

    @NotBlank(message = "Passenger name is required.")
    @Size(max = 120, message = "Passenger name cannot exceed 120 characters.")
    private String passenger;

    @NotBlank(message = "Cabin is required.")
    @Pattern(
            regexp = "(?i)ECONOMY",
            message = "Only ECONOMY cabin is currently available.")
    private String cabin;

    public CreateBookingRequestDTO() {
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public String getCabin() {
        return cabin;
    }

    public void setCabin(String cabin) {
        this.cabin = cabin;
    }
}
