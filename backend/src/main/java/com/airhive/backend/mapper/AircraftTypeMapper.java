package com.airhive.backend.mapper;

import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.entity.AircraftType;

public final class AircraftTypeMapper {

    private AircraftTypeMapper() {
    }

    public static AircraftTypeResponseDTO toResponse(
            AircraftType aircraftType) {

        AircraftTypeResponseDTO dto =
                new AircraftTypeResponseDTO();

        dto.setId(aircraftType.getId());
        dto.setTypeCode(aircraftType.getTypeCode());
        dto.setManufacturer(aircraftType.getManufacturer());
        dto.setModel(aircraftType.getModel());
        dto.setPassengerCapacity(
                aircraftType.getPassengerCapacity());
        dto.setCrewCapacity(
                aircraftType.getCrewCapacity());

        return dto;
    }
}