package com.airhive.backend.mapper;

import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.entity.Flight;

public final class FlightMapper {

    private FlightMapper() {
    }

    public static FlightResponseDTO toResponse(Flight flight) {

        FlightResponseDTO dto = new FlightResponseDTO();

        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());

        dto.setAircraftId(
                flight.getAircraft().getId()
        );

        dto.setAircraftRegistration(
                flight.getAircraft().getRegistrationNumber()
        );

        dto.setRouteId(
                flight.getRoute().getId()
        );

        dto.setDepartureAirportId(
                flight.getDepartureAirport().getId()
        );

        dto.setDepartureAirportCode(
                flight.getDepartureAirport().getIataCode()
        );

        dto.setArrivalAirportId(
                flight.getArrivalAirport().getId()
        );

        dto.setArrivalAirportCode(
                flight.getArrivalAirport().getIataCode()
        );

        dto.setScheduledDeparture(
                flight.getScheduledDeparture()
        );

        dto.setScheduledArrival(
                flight.getScheduledArrival()
        );

        dto.setStatus(
                flight.getStatus()
        );

        return dto;
    }
}