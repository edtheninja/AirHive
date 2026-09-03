package com.airhive.backend.dto;

public class FlightEventDTO {

    private String eventType;
    private FlightResponseDTO flight;

    public FlightEventDTO() {
    }

    public FlightEventDTO(String eventType, FlightResponseDTO flight) {
        this.eventType = eventType;
        this.flight = flight;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public FlightResponseDTO getFlight() {
        return flight;
    }

    public void setFlight(FlightResponseDTO flight) {
        this.flight = flight;
    }
}