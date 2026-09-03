package com.airhive.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.airhive.backend.dto.FlightEventDTO;
import com.airhive.backend.dto.FlightResponseDTO;

class FlightWebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private FlightWebSocketService flightWebSocketService;

    private FlightResponseDTO flight;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);

        flightWebSocketService = new FlightWebSocketService(
                messagingTemplate);

        flight = new FlightResponseDTO();
        flight.setId(1L);
        flight.setFlightNumber("AH101");
        flight.setStatus("SCHEDULED");
    }

    @Test
    void publishFlightCreated_shouldSendCreatedEvent() {
        flightWebSocketService.publishFlightCreated(flight);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/flights"),
                any(FlightEventDTO.class));
    }

    @Test
    void publishFlightUpdated_shouldSendUpdatedEvent() {
        flightWebSocketService.publishFlightUpdated(flight);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/flights"),
                any(FlightEventDTO.class));
    }

    @Test
    void publishFlightDeleted_shouldSendDeletedEvent() {
        flightWebSocketService.publishFlightDeleted(flight);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/flights"),
                any(FlightEventDTO.class));
    }
}