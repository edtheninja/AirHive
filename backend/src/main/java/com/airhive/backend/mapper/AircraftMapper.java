package com.airhive.backend.mapper;

import com.airhive.backend.dto.AircraftResponseDTO;
import com.airhive.backend.entity.Aircraft;

public final class AircraftMapper {

    private AircraftMapper() {
    }

    public static AircraftResponseDTO toResponse(Aircraft aircraft) {

        AircraftResponseDTO dto = new AircraftResponseDTO();

        dto.setId(aircraft.getId());

        dto.setRegistrationNumber(
                aircraft.getRegistrationNumber()
        );

        dto.setAircraftTypeId(
                aircraft.getAircraftType().getId()
        );

        dto.setAircraftTypeCode(
                aircraft.getAircraftType().getTypeCode()
        );

        dto.setStatus(
                aircraft.getStatus()
        );

        return dto;
    }
}