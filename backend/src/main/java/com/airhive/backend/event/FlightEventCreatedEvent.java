package com.airhive.backend.event;

import com.airhive.backend.dto.FlightEventDTO;

public record FlightEventCreatedEvent(
        FlightEventDTO event) {
}