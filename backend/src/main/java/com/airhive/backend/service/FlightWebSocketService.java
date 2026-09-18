package com.airhive.backend.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.FlightEventDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.event.FlightEventCreatedEvent;

@Service
public class FlightWebSocketService {

    private static final String FLIGHTS_TOPIC = "/topic/flights";

    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public FlightWebSocketService(
            ApplicationEventPublisher eventPublisher,
            SimpMessagingTemplate messagingTemplate) {

        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    public void publishFlightCreated(
            FlightResponseDTO flight) {

        publish("FLIGHT_CREATED", flight);
    }

    public void publishFlightUpdated(
            FlightResponseDTO flight) {

        publish("FLIGHT_UPDATED", flight);
    }

    public void publishFlightDeleted(
            FlightResponseDTO flight) {

        publish("FLIGHT_DELETED", flight);
    }

    public void publishEvent(
            FlightEventDTO event) {

        messagingTemplate.convertAndSend(
                FLIGHTS_TOPIC,
                event);
    }

    private void publish(
            String eventType,
            FlightResponseDTO flight) {

        eventPublisher.publishEvent(
                new FlightEventCreatedEvent(
                        new FlightEventDTO(eventType, flight)));
    }
}