package com.airhive.backend.mapper;

import com.airhive.backend.dto.PassengerResponseDTO;
import com.airhive.backend.entity.Passenger;

public final class PassengerMapper {

    private PassengerMapper() {
    }

    public static PassengerResponseDTO toResponse(Passenger passenger) {

        PassengerResponseDTO dto = new PassengerResponseDTO();

        dto.setId(passenger.getId());
        dto.setPassengerCode(passenger.getPassengerCode());
        dto.setName(passenger.getName());
        dto.setTier(passenger.getTier());
        dto.setFlight(passenger.getFlight());
        dto.setRoute(passenger.getRoute());
        dto.setSeat(passenger.getSeat());
        dto.setBags(passenger.getBags());
        dto.setCheckedIn(passenger.isCheckedIn());

        return dto;
    }
}
