package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;

@Service
public class FlightService {

        private final FlightRepository flightRepository;
        private final AircraftRepository aircraftRepository;
        private final AirportRepository airportRepository;
        private final RouteRepository routeRepository;

        public FlightService(
                        FlightRepository flightRepository,
                        AircraftRepository aircraftRepository,
                        AirportRepository airportRepository,
                        RouteRepository routeRepository) {

                this.flightRepository = flightRepository;
                this.aircraftRepository = aircraftRepository;
                this.airportRepository = airportRepository;
                this.routeRepository = routeRepository;

        }

        public List<Flight> getAllFlights() {
                return flightRepository.findAll();
        }

        public Flight getFlightById(Long id) {
                return flightRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Flight not found with id: " + id));
        }

        public Flight createFlight(Flight flight) {

                validateFlight(flight);

                if (flightRepository.existsByFlightNumber(
                                flight.getFlightNumber())) {

                        throw new DuplicateResourceException(
                                        "Flight already exists: " + flight.getFlightNumber());
                }

                Long routeId = flight.getRoute().getId();

                Route route = routeRepository.findById(routeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with id: " + routeId));

                if (!route.getDepartureAirport().getId()
                                .equals(flight.getDepartureAirport().getId())) {

                        throw new RuntimeException(
                                        "Flight departure airport does not match route");
                }

                if (!route.getArrivalAirport().getId()
                                .equals(flight.getArrivalAirport().getId())) {

                        throw new RuntimeException(
                                        "Flight arrival airport does not match route");
                }

                flight.setRoute(route);

                return flightRepository.save(flight);
        }

        public Flight updateFlight(Long id, Flight updatedFlight) {

                Flight existingFlight = getFlightById(id);

                validateFlight(updatedFlight);

                if (!existingFlight.getFlightNumber()
                                .equals(updatedFlight.getFlightNumber())
                                && flightRepository.existsByFlightNumber(
                                                updatedFlight.getFlightNumber())) {

                        throw new DuplicateResourceException(
                                        "Flight already exists: "
                                                        + updatedFlight.getFlightNumber());
                }

                Long routeId = updatedFlight.getRoute().getId();

                Route route = routeRepository.findById(routeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with id: " + routeId));

                if (!route.getDepartureAirport().getId()
                                .equals(updatedFlight.getDepartureAirport().getId())) {

                        throw new RuntimeException(
                                        "Flight departure airport does not match route");
                }

                if (!route.getArrivalAirport().getId()
                                .equals(updatedFlight.getArrivalAirport().getId())) {

                        throw new RuntimeException(
                                        "Flight arrival airport does not match route");
                }

                existingFlight.setFlightNumber(
                                updatedFlight.getFlightNumber());

                existingFlight.setAircraft(
                                updatedFlight.getAircraft());

                existingFlight.setRoute(route);

                existingFlight.setDepartureAirport(
                                updatedFlight.getDepartureAirport());

                existingFlight.setArrivalAirport(
                                updatedFlight.getArrivalAirport());

                existingFlight.setScheduledDeparture(
                                updatedFlight.getScheduledDeparture());

                existingFlight.setScheduledArrival(
                                updatedFlight.getScheduledArrival());

                existingFlight.setStatus(
                                updatedFlight.getStatus());

                return flightRepository.save(existingFlight);
        }

        public void deleteFlight(Long id) {

                Flight flight = getFlightById(id);

                flightRepository.delete(flight);
        }

        private void validateFlight(Flight flight) {

                if (flight.getFlightNumber() == null
                                || flight.getFlightNumber().isBlank()) {

                        throw new RuntimeException(
                                        "Flight number is required");
                }

                if (flight.getAircraft() == null
                                || flight.getAircraft().getId() == null) {

                        throw new RuntimeException(
                                        "Aircraft is required");
                }

                if (!aircraftRepository.existsById(
                                flight.getAircraft().getId())) {

                        throw new RuntimeException(
                                        "Aircraft not found with id: "
                                                        + flight.getAircraft().getId());
                }

                if (flight.getDepartureAirport() == null
                                || flight.getDepartureAirport().getId() == null) {

                        throw new RuntimeException(
                                        "Departure airport is required");
                }

                if (!airportRepository.existsById(
                                flight.getDepartureAirport().getId())) {

                        throw new RuntimeException(
                                        "Departure airport not found with id: "
                                                        + flight.getDepartureAirport().getId());
                }

                if (flight.getArrivalAirport() == null
                                || flight.getArrivalAirport().getId() == null) {

                        throw new RuntimeException(
                                        "Arrival airport is required");
                }

                if (!airportRepository.existsById(
                                flight.getArrivalAirport().getId())) {

                        throw new RuntimeException(
                                        "Arrival airport not found with id: "
                                                        + flight.getArrivalAirport().getId());
                }

                if (flight.getDepartureAirport().getId()
                                .equals(flight.getArrivalAirport().getId())) {

                        throw new RuntimeException(
                                        "Departure and arrival airports cannot be the same");
                }

                if (flight.getScheduledDeparture() == null
                                || flight.getScheduledArrival() == null) {

                        throw new RuntimeException(
                                        "Scheduled departure and arrival are required");
                }

                if (!flight.getScheduledArrival()
                                .isAfter(flight.getScheduledDeparture())) {

                        throw new RuntimeException(
                                        "Scheduled arrival must be after scheduled departure");
                }

                if (flight.getStatus() == null
                                || flight.getStatus().isBlank()) {

                        throw new RuntimeException(
                                        "Flight status is required");
                }
        }
}