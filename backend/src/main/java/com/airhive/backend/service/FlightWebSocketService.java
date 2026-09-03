package com.airhive.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.FlightEventDTO;
import com.airhive.backend.dto.FlightResponseDTO;

@Service
public class FlightWebSocketService {

    private static final String FLIGHTS_TOPIC = "/topic/flights";

    private final SimpMessagingTemplate messagingTemplate;

    public FlightWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishFlightCreated(FlightResponseDTO flight) {
        publish("FLIGHT_CREATED", flight);
    }

    public void publishFlightUpdated(FlightResponseDTO flight) {
        publish("FLIGHT_UPDATED", flight);
    }

    public void publishFlightDeleted(FlightResponseDTO flight) {
        publish("FLIGHT_DELETED", flight);
    }

    private void publish(String eventType, FlightResponseDTO flight) {
        FlightEventDTO event = new FlightEventDTO(eventType, flight);
        messagingTemplate.convertAndSend(FLIGHTS_TOPIC, event);
    }
}