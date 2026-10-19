package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.BookingResponseDTO;
import com.airhive.backend.entity.Booking;
import com.airhive.backend.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository);
    }

    @Test
    void getAllBookingsReturnsMappedBookings() {
        Booking booking = new Booking();
        booking.setPnr("AH123456");
        booking.setCabin("Economy");
        booking.setSeat("12A");
        booking.setStatus("CONFIRMED");
        booking.setAmount(new BigDecimal("5500.00"));
        booking.setTravelDate(LocalDate.of(2026, 11, 15));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.getAllBookings();

        assertEquals(1, result.size());
        assertEquals("AH123456", result.get(0).getPnr());
        assertEquals("Economy", result.get(0).getCabin());
        assertEquals("12A", result.get(0).getSeat());
        assertEquals("CONFIRMED", result.get(0).getStatus());
        assertEquals(new BigDecimal("5500.00"), result.get(0).getAmount());
        assertEquals(LocalDate.of(2026, 11, 15), result.get(0).getTravelDate());

        verify(bookingRepository).findAll();
    }

    @Test
    void getAllBookingsReturnsEmptyListWhenNoBookingsExist() {
        when(bookingRepository.findAll()).thenReturn(List.of());

        List<BookingResponseDTO> result = bookingService.getAllBookings();

        assertEquals(0, result.size());
        verify(bookingRepository).findAll();
    }
}