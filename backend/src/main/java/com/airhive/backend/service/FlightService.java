package com.airhive.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.FlightRequestDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.FlightMapper;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AirportRepository;
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
        private final FlightWebSocketService flightWebSocketService;

        public FlightService(
                        FlightRepository flightRepository,
                        AircraftRepository aircraftRepository,
                        AirportRepository airportRepository,
                        RouteRepository routeRepository,
                        FlightWebSocketService flightWebSocketService) {

                this.flightRepository = flightRepository;
                this.aircraftRepository = aircraftRepository;
                this.airportRepository = airportRepository;
                this.routeRepository = routeRepository;
                this.flightWebSocketService = flightWebSocketService;
        }

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
                flightWebSocketService.publishFlightCreated(response);

                return response;
        }

        @Cacheable(value = "flights", key = "#id")
        public FlightResponseDTO getFlightById(Long id) {

                Flight flight = flightRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Flight not found with id: " + id));

                return FlightMapper.toResponse(flight);
        }

        @Cacheable("flights")
        public List<FlightResponseDTO> getAllFlights() {

                return flightRepository.findAll()
                                .stream()
                                .map(FlightMapper::toResponse)
                                .toList();
        }

        @CacheEvict(value = "flights", allEntries = true)
        public FlightResponseDTO updateFlight(
                        Long id,
                        FlightRequestDTO request) {

                Flight existingFlight = getFlightEntityById(id);

                validateRequest(request);

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

                existingFlight.setStatus(
                                request.getStatus());

                Flight updatedFlight = flightRepository.save(existingFlight);

                FlightResponseDTO response = FlightMapper.toResponse(updatedFlight);
                flightWebSocketService.publishFlightUpdated(response);

                return response;
        }

        @CacheEvict(value = "flights", allEntries = true)
        public void deleteFlight(Long id) {

                Flight flight = getFlightEntityById(id);

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
}