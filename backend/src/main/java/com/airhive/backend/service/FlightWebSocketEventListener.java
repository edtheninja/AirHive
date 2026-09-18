package com.airhive.backend.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.airhive.backend.event.FlightEventCreatedEvent;

@Component
public class FlightWebSocketEventListener {

    private final FlightWebSocketService flightWebSocketService;

    public FlightWebSocketEventListener(
            FlightWebSocketService flightWebSocketService) {

        this.flightWebSocketService = flightWebSocketService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFlightEvent(
            FlightEventCreatedEvent event) {

        flightWebSocketService.publishEvent(
                event.event());
    }
}