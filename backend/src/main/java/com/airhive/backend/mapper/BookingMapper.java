package com.airhive.backend.mapper;

import com.airhive.backend.dto.BookingResponseDTO;
import com.airhive.backend.entity.Booking;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponseDTO toResponse(Booking booking) {
        BookingResponseDTO response = new BookingResponseDTO();

        response.setId(booking.getId());
        response.setPnr(booking.getPnr());
        response.setPassenger(booking.getPassenger());
        response.setFlight(booking.getFlight());
        response.setRoute(booking.getRoute());
        response.setCabin(booking.getCabin());
        response.setSeat(booking.getSeat());
        response.setStatus(booking.getStatus());
        response.setAmount(booking.getAmount());
        response.setTravelDate(booking.getTravelDate());

        return response;
    }
}
