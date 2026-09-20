package com.airhive.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.FlightRequestDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.dto.FlightStatusUpdateRequestDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceInUseException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.FlightMapper;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightActivityRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;

@Service
public class FlightService {
        private static final Set<String> VALID_FLIGHT_STATUSES = Set.of(
                        "SCHEDULED",
                        "BOARDING",
                        "TAXIING",
                        "DEPARTED",
                        "IN AIR",
                        "LANDED",
                        "DELAYED",
                        "CANCELLED",
                        "LANDING");
        private final FlightRepository flightRepository;
        private final AircraftRepository aircraftRepository;
        private final AirportRepository airportRepository;
        private final RouteRepository routeRepository;
        private final FlightActivityRepository flightActivityRepository;
        private final FlightWebSocketService flightWebSocketService;
        private final FlightNotificationService flightNotificationService;
        private final FlightActivityService flightActivityService;

        public FlightService(
                        FlightRepository flightRepository,
                        AircraftRepository aircraftRepository,
                        AirportRepository airportRepository,
                        RouteRepository routeRepository,
                        FlightActivityRepository flightActivityRepository,
                        FlightWebSocketService flightWebSocketService,
                        FlightNotificationService flightNotificationService,
                        FlightActivityService flightActivityService) {

                this.flightRepository = flightRepository;
                this.aircraftRepository = aircraftRepository;
                this.airportRepository = airportRepository;
                this.routeRepository = routeRepository;
                this.flightActivityRepository = flightActivityRepository;
                this.flightWebSocketService = flightWebSocketService;
                this.flightNotificationService = flightNotificationService;
                this.flightActivityService = flightActivityService;
        }

        @Transactional
        @CacheEvict(value = "flights", allEntries = true)
        public FlightResponseDTO createFlight(FlightRequestDTO request) {

                validateRequest(request);
                if (request.getStatus() == null
                                || request.getStatus().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Flight status is required");
                }
                if (!VALID_FLIGHT_STATUSES.contains(
                                request.getStatus().trim().toUpperCase())) {

                        throw new IllegalArgumentException(
                                        "Invalid flight status: " + request.getStatus());
                }

                if (flightRepository.existsByFlightNumber(
                                request.getFlightNumber())) {

                        throw new DuplicateResourceException(
                                        "Flight already exists: "
                                                        + request.getFlightNumber());
                }

                Aircraft aircraft = aircraftRepository.findById(request.getAircraftId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Aircraft not found with id: "
                                                                + request.getAircraftId()));

                Airport departureAirport = airportRepository.findById(
                                request.getDepartureAirportId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Departure airport not found with id: "
                                                                + request.getDepartureAirportId()));

                Airport arrivalAirport = airportRepository.findById(
                                request.getArrivalAirportId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Arrival airport not found with id: "
                                                                + request.getArrivalAirportId()));

                Route route = routeRepository.findById(request.getRouteId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with id: "
                                                                + request.getRouteId()));

                validateRouteAirports(
                                route,
                                departureAirport,
                                arrivalAirport);

                long conflictCount = flightRepository.countAircraftScheduleConflicts(
                                request.getAircraftId(),
                                request.getScheduledArrival(),
                                request.getScheduledDeparture());

                if (conflictCount > 0) {
                        throw new DuplicateResourceException(
                                        "Aircraft is already scheduled for another flight during this time");
                }

                Flight flight = new Flight();

                flight.setFlightNumber(
                                request.getFlightNumber());

                flight.setAircraft(aircraft);

                flight.setRoute(route);

                flight.setDepartureAirport(
                                departureAirport);

                flight.setArrivalAirport(
                                arrivalAirport);

                flight.setScheduledDeparture(
                                request.getScheduledDeparture());

                flight.setScheduledArrival(
                                request.getScheduledArrival());

                flight.setStatus(
                                request.getStatus());

                Flight savedFlight = flightRepository.save(flight);

                FlightResponseDTO response = FlightMapper.toResponse(savedFlight);

                flightActivityService.recordFlightCreated(savedFlight);
                flightWebSocketService.publishFlightCreated(response);
                return response;
        }

        public FlightResponseDTO getFlightById(Long id) {

                Flight flight = flightRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Flight not found with id: " + id));

                return FlightMapper.toResponse(flight);
        }

        public List<FlightResponseDTO> getAllFlights() {

                return flightRepository.findAll()
                                .stream()
                                .map(FlightMapper::toResponse)
                                .toList();
        }

        @Transactional
        @CacheEvict(value = "flights", allEntries = true)
        public FlightResponseDTO updateFlight(
                        Long id,
                        FlightRequestDTO request) {

                Flight existingFlight = getFlightEntityById(id);
                String previousStatus = existingFlight.getStatus();

                validateRequest(request);

                String newStatus = request.getStatus()
                                .trim()
                                .toUpperCase();

                if (!VALID_FLIGHT_STATUSES.contains(newStatus)) {
                        throw new IllegalArgumentException(
                                        "Invalid flight status: " + request.getStatus());
                }

                validateStatusTransition(previousStatus, newStatus);

                if (!existingFlight.getFlightNumber()
                                .equals(request.getFlightNumber())
                                && flightRepository.existsByFlightNumber(
                                                request.getFlightNumber())) {

                        throw new DuplicateResourceException(
                                        "Flight already exists: "
                                                        + request.getFlightNumber());
                }

                Aircraft aircraft = aircraftRepository.findById(
                                request.getAircraftId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Aircraft not found with id: "
                                                                + request.getAircraftId()));

                Airport departureAirport = airportRepository.findById(
                                request.getDepartureAirportId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Departure airport not found with id: "
                                                                + request.getDepartureAirportId()));

                Airport arrivalAirport = airportRepository.findById(
                                request.getArrivalAirportId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Arrival airport not found with id: "
                                                                + request.getArrivalAirportId()));

                Route route = routeRepository.findById(
                                request.getRouteId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with id: "
                                                                + request.getRouteId()));

                validateRouteAirports(
                                route,
                                departureAirport,
                                arrivalAirport);

                long conflictCount = flightRepository.countAircraftScheduleConflictsForUpdate(
                                request.getAircraftId(),
                                id,
                                request.getScheduledArrival(),
                                request.getScheduledDeparture());

                if (conflictCount > 0) {
                        throw new DuplicateResourceException(
                                        "Aircraft is already scheduled for another flight during this time");
                }

                existingFlight.setFlightNumber(
                                request.getFlightNumber());

                existingFlight.setAircraft(
                                aircraft);

                existingFlight.setDepartureAirport(
                                departureAirport);

                existingFlight.setArrivalAirport(
                                arrivalAirport);

                existingFlight.setRoute(
                                route);

                existingFlight.setScheduledDeparture(
                                request.getScheduledDeparture());

                existingFlight.setScheduledArrival(
                                request.getScheduledArrival());

                existingFlight.setStatus(newStatus);

                Flight updatedFlight = flightRepository.save(existingFlight);

                FlightResponseDTO response = FlightMapper.toResponse(updatedFlight);

                flightNotificationService.notifyFlightStatusChange(
                                response,
                                previousStatus);

                flightActivityService.recordStatusChange(
                                updatedFlight,
                                previousStatus,
                                updatedFlight.getStatus());

                flightWebSocketService.publishFlightUpdated(response);
                return response;
        }

        @Transactional
        @CacheEvict(value = "flights", allEntries = true)
        public FlightResponseDTO updateFlightStatus(
                        Long id,
                        FlightStatusUpdateRequestDTO request) {

                if (request == null
                                || request.getStatus() == null
                                || request.getStatus().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Flight status is required");
                }

                String newStatus = request.getStatus()
                                .trim()
                                .toUpperCase();

                if (!VALID_FLIGHT_STATUSES.contains(newStatus)) {
                        throw new IllegalArgumentException(
                                        "Invalid flight status: " + request.getStatus());
                }

                if ((newStatus.equals("DELAYED")
                                || newStatus.equals("CANCELLED"))
                                && (request.getReason() == null
                                                || request.getReason().isBlank())) {

                        throw new IllegalArgumentException(
                                        "A reason is required for delayed or cancelled flights");
                }

                Flight existingFlight = getFlightEntityById(id);
                String previousStatus = existingFlight.getStatus();

                validateStatusTransition(previousStatus, newStatus);

                existingFlight.setStatus(newStatus);
                existingFlight.setStatus(newStatus);

                Flight updatedFlight = flightRepository.save(existingFlight);

                FlightResponseDTO response = FlightMapper.toResponse(updatedFlight);

                flightNotificationService.notifyFlightStatusChange(
                                response,
                                previousStatus);

                flightActivityService.recordStatusChange(
                                updatedFlight,
                                previousStatus,
                                newStatus,
                                request.getReason());

                flightWebSocketService.publishFlightUpdated(response);

                return response;
        }

        @Transactional
        @CacheEvict(value = "flights", allEntries = true)
        public void deleteFlight(Long id) {

                Flight flight = getFlightEntityById(id);

                if (flightActivityRepository.existsByFlightId(id)) {
                        throw new ResourceInUseException(
                                        "Flight cannot be deleted because it has associated activity records");
                }

                FlightResponseDTO response = FlightMapper.toResponse(flight);

                flightRepository.delete(flight);

                flightWebSocketService.publishFlightDeleted(response);
        }

        private Flight getFlightEntityById(Long id) {

                return flightRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Flight not found with id: " + id));
        }

        private void validateRequest(
                        FlightRequestDTO request) {

                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Flight request is required");
                }

                if (request.getFlightNumber() == null
                                || request.getFlightNumber().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Flight number is required");
                }

                if (request.getAircraftId() == null) {

                        throw new IllegalArgumentException(
                                        "Aircraft is required");
                }

                if (request.getDepartureAirportId() == null) {

                        throw new IllegalArgumentException(
                                        "Departure airport is required");
                }

                if (request.getArrivalAirportId() == null) {

                        throw new IllegalArgumentException(
                                        "Arrival airport is required");
                }

                if (request.getRouteId() == null) {

                        throw new IllegalArgumentException(
                                        "Route is required");
                }

                if (request.getScheduledDeparture() == null) {

                        throw new IllegalArgumentException(
                                        "Scheduled departure is required");
                }

                if (request.getScheduledArrival() == null) {

                        throw new IllegalArgumentException(
                                        "Scheduled arrival is required");
                }

                if (!request.getScheduledDeparture()
                                .isBefore(request.getScheduledArrival())) {

                        throw new IllegalArgumentException(
                                        "Scheduled departure must be before scheduled arrival");
                }

                if (request.getStatus() == null
                                || request.getStatus().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Flight status is required");
                }

                if (request.getDepartureAirportId()
                                .equals(request.getArrivalAirportId())) {

                        throw new IllegalArgumentException(
                                        "Departure and arrival airports cannot be the same");
                }
        }

        private void validateRouteAirports(
                        Route route,
                        Airport departureAirport,
                        Airport arrivalAirport) {

                if (!route.getDepartureAirport().getId()
                                .equals(departureAirport.getId())) {

                        throw new IllegalArgumentException(
                                        "Flight departure airport does not match route");
                }

                if (!route.getArrivalAirport().getId()
                                .equals(arrivalAirport.getId())) {

                        throw new IllegalArgumentException(
                                        "Flight arrival airport does not match route");
                }
        }

        private void validateStatusTransition(
                        String previousStatus,
                        String newStatus) {

                if (previousStatus.equals(newStatus)) {
                        return;
                }

                boolean validTransition = switch (previousStatus) {
                        case "SCHEDULED" ->
                                newStatus.equals("BOARDING")
                                                || newStatus.equals("DELAYED")
                                                || newStatus.equals("CANCELLED");

                        case "BOARDING" ->
                                newStatus.equals("TAXIING")
                                                || newStatus.equals("DELAYED")
                                                || newStatus.equals("CANCELLED");

                        case "TAXIING" ->
                                newStatus.equals("DEPARTED")
                                                || newStatus.equals("DELAYED")
                                                || newStatus.equals("CANCELLED");

                        case "DELAYED" ->
                                newStatus.equals("BOARDING")
                                                || newStatus.equals("CANCELLED");
                        case "DEPARTED" ->
                                newStatus.equals("IN AIR");

                        case "IN AIR" ->
                                newStatus.equals("LANDING");

                        case "LANDING" ->
                                newStatus.equals("LANDED");

                        case "LANDED", "CANCELLED" ->
                                false;

                        default ->
                                false;
                };

                if (!validTransition) {
                        throw new IllegalArgumentException(
                                        "Invalid flight status transition from "
                                                        + previousStatus
                                                        + " to "
                                                        + newStatus);
                }
        }
}