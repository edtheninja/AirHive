package com.airhive.backend.mapper;

import com.airhive.backend.dto.AirportResponseDTO;
import com.airhive.backend.entity.Airport;

public final class AirportMapper {

    private AirportMapper() {
    }

    public static AirportResponseDTO toResponse(Airport airport) {

        AirportResponseDTO dto = new AirportResponseDTO();

        dto.setId(airport.getId());
        dto.setIataCode(airport.getIataCode());
        dto.setIcaoCode(airport.getIcaoCode());
        dto.setName(airport.getName());
        dto.setCity(airport.getCity());
        dto.setCountry(airport.getCountry());
        dto.setTerminalCount(airport.getTerminalCount());
        dto.setStatus(airport.getStatus());

        return dto;
    }
}