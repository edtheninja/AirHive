package com.airhive.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.airhive.backend.dto.FlightEventDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.event.FlightEventCreatedEvent;

class FlightWebSocketServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private FlightWebSocketService flightWebSocketService;

    private FlightResponseDTO flight;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);

        flightWebSocketService = new FlightWebSocketService(
                eventPublisher,
                messagingTemplate);

        flight = new FlightResponseDTO();
        flight.setId(1L);
        flight.setFlightNumber("AH101");
        flight.setStatus("SCHEDULED");
    }

    @Test
    void publishFlightCreated_shouldPublishCreatedEvent() {
        flightWebSocketService.publishFlightCreated(flight);

        verify(eventPublisher).publishEvent(
                any(FlightEventCreatedEvent.class));
    }

    @Test
    void publishFlightUpdated_shouldPublishUpdatedEvent() {
        flightWebSocketService.publishFlightUpdated(flight);

        verify(eventPublisher).publishEvent(
                any(FlightEventCreatedEvent.class));
    }

    @Test
    void publishFlightDeleted_shouldPublishDeletedEvent() {
        flightWebSocketService.publishFlightDeleted(flight);

        verify(eventPublisher).publishEvent(
                any(FlightEventCreatedEvent.class));
    }

    @Test
    void publishEvent_shouldSendMessageToFlightsTopic() {
        FlightEventDTO event =
                new FlightEventDTO("FLIGHT_CREATED", flight);

        flightWebSocketService.publishEvent(event);

        verify(messagingTemplate).convertAndSend(
                "/topic/flights",
                event);
    }
}