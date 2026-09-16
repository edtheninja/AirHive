package com.airhive.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.dto.FlightActivityResponseDTO;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.FlightActivity;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.FlightActivityRepository;
import com.airhive.backend.repository.FlightRepository;

@Service
public class FlightActivityService {
    private final FlightActivityRepository flightActivityRepository;
    private final FlightRepository flightRepository;

    public FlightActivityService(FlightActivityRepository flightActivityRepository, FlightRepository flightRepository) {
        this.flightActivityRepository = flightActivityRepository;
        this.flightRepository = flightRepository;
    }

    public List<FlightActivityResponseDTO> getActivities(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException("Flight not found with id: " + flightId);
        }
        return flightActivityRepository.findByFlightIdOrderByCreatedAtDesc(flightId).stream().map(this::toResponse)
                .toList();
    }

    public void recordStatusChange(
            Flight flight,
            String previousStatus,
            String currentStatus) {

        recordStatusChange(flight, previousStatus, currentStatus, null);
    }

    public void recordStatusChange(
            Flight flight,
            String previousStatus,
            String currentStatus,
            String reason) {

        if (previousStatus == null
                || currentStatus == null
                || previousStatus.equalsIgnoreCase(currentStatus)) {
            return;
        }

        FlightActivity activity = new FlightActivity();
        activity.setFlight(flight);
        activity.setEventType("STATUS_CHANGED");
        activity.setPreviousStatus(previousStatus);
        activity.setCurrentStatus(currentStatus);
        activity.setReason(reason);
        activity.setMessage(String.format(
                "Flight %s status changed from %s to %s.",
                flight.getFlightNumber(),
                previousStatus,
                currentStatus));
        activity.setCreatedAt(LocalDateTime.now());

        flightActivityRepository.save(activity);
    }

    public void recordFlightCreated(Flight flight) {
        FlightActivity activity = new FlightActivity();
        activity.setFlight(flight);
        activity.setEventType("FLIGHT_CREATED");
        activity.setCurrentStatus(flight.getStatus());
        activity.setMessage(String.format("Flight %s was created.", flight.getFlightNumber()));
        activity.setCreatedAt(LocalDateTime.now());
        flightActivityRepository.save(activity);
    }

    private FlightActivityResponseDTO toResponse(FlightActivity activity) {
        return new FlightActivityResponseDTO(activity.getId(), activity.getEventType(), activity.getMessage(),
                activity.getPreviousStatus(), activity.getCurrentStatus(), activity.getReason(),
                activity.getCreatedAt());
    }
}