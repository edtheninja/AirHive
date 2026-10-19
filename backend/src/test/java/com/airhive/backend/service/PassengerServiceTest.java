package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.PassengerResponseDTO;
import com.airhive.backend.entity.Passenger;
import com.airhive.backend.repository.PassengerRepository;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    private PassengerService passengerService;

    @BeforeEach
    void setUp() {
        passengerService = new PassengerService(passengerRepository);
    }

    @Test
    void getAllPassengersReturnsMappedPassengers() {
        Passenger passenger = new Passenger();
        passenger.setPassengerCode("PAX-1001");
        passenger.setName("Jordan Lee");
        passenger.setTier("Gold");
        passenger.setSeat("14C");
        passenger.setBags(2);
        passenger.setCheckedIn(true);

        when(passengerRepository.findAll()).thenReturn(List.of(passenger));

        List<PassengerResponseDTO> result =
                passengerService.getAllPassengers();

        assertEquals(1, result.size());
        assertEquals("PAX-1001", result.get(0).getPassengerCode());
        assertEquals("Jordan Lee", result.get(0).getName());
        assertEquals("Gold", result.get(0).getTier());
        assertEquals("14C", result.get(0).getSeat());
        assertEquals(2, result.get(0).getBags());
        assertEquals(true, result.get(0).isCheckedIn());

        verify(passengerRepository).findAll();
    }

    @Test
    void getAllPassengersReturnsEmptyListWhenNoPassengersExist() {
        when(passengerRepository.findAll()).thenReturn(List.of());

        List<PassengerResponseDTO> result =
                passengerService.getAllPassengers();

        assertEquals(0, result.size());
        verify(passengerRepository).findAll();
    }
}
