package com.airhive.backend.service;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Airport;
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

    public Flight createFlight(Flight flight) {

        validateFlight(flight);

        if (flightRepository.existsByFlightNumber(
                flight.getFlightNumber())) {

            throw new DuplicateResourceException(
                    "Flight already exists: "
                            + flight.getFlightNumber());
        }

        Long aircraftId = flight.getAircraft().getId();

        Aircraft aircraft = aircraftRepository.findById(aircraftId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aircraft not found with id: " + aircraftId));

        flight.setAircraft(aircraft);

        Long departureAirportId =
                flight.getDepartureAirport().getId();

        Airport departureAirport =
                airportRepository.findById(departureAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Departure airport not found with id: "
                                                + departureAirportId));

        flight.setDepartureAirport(departureAirport);

        Long arrivalAirportId =
                flight.getArrivalAirport().getId();

        Airport arrivalAirport =
                airportRepository.findById(arrivalAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Arrival airport not found with id: "
                                                + arrivalAirportId));

        flight.setArrivalAirport(arrivalAirport);

        Long routeId = flight.getRoute().getId();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + routeId));

        if (!route.getDepartureAirport().getId()
                .equals(departureAirportId)) {

            throw new RuntimeException(
                    "Flight departure airport does not match route");
        }

        if (!route.getArrivalAirport().getId()
                .equals(arrivalAirportId)) {

            throw new RuntimeException(
                    "Flight arrival airport does not match route");
        }

        flight.setRoute(route);

        // Aircraft scheduling conflict check
        long conflictCount =
                flightRepository.countAircraftScheduleConflicts(
                        aircraftId,
                        flight.getScheduledArrival(),
                        flight.getScheduledDeparture());

        if (conflictCount > 0) {
            throw new RuntimeException(
                    "Aircraft is already scheduled for another flight during this time");
        }

        return flightRepository.save(flight);
    }

    public Flight getFlightById(Long id) {

        return flightRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Flight not found with id: " + id));
    }

    public java.util.List<Flight> getAllFlights() {

        return flightRepository.findAll();
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

        Long aircraftId =
                updatedFlight.getAircraft().getId();

        Aircraft aircraft =
                aircraftRepository.findById(aircraftId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aircraft not found with id: "
                                                + aircraftId));

        Long departureAirportId =
                updatedFlight.getDepartureAirport().getId();

        Airport departureAirport =
                airportRepository.findById(departureAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Departure airport not found with id: "
                                                + departureAirportId));

        Long arrivalAirportId =
                updatedFlight.getArrivalAirport().getId();

        Airport arrivalAirport =
                airportRepository.findById(arrivalAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Arrival airport not found with id: "
                                                + arrivalAirportId));

        Long routeId =
                updatedFlight.getRoute().getId();

        Route route =
                routeRepository.findById(routeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Route not found with id: "
                                                + routeId));

        if (!route.getDepartureAirport().getId()
                .equals(departureAirportId)) {

            throw new RuntimeException(
                    "Flight departure airport does not match route");
        }

        if (!route.getArrivalAirport().getId()
                .equals(arrivalAirportId)) {

            throw new RuntimeException(
                    "Flight arrival airport does not match route");
        }

        // Aircraft scheduling conflict check
        long conflictCount =
                flightRepository.countAircraftScheduleConflictsForUpdate(
                        aircraftId,
                        id,
                        updatedFlight.getScheduledArrival(),
                        updatedFlight.getScheduledDeparture());

        if (conflictCount > 0) {
            throw new RuntimeException(
                    "Aircraft is already scheduled for another flight during this time");
        }

        existingFlight.setFlightNumber(
                updatedFlight.getFlightNumber());

        existingFlight.setAircraft(aircraft);

        existingFlight.setDepartureAirport(
                departureAirport);

        existingFlight.setArrivalAirport(
                arrivalAirport);

        existingFlight.setRoute(route);

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

            throw new IllegalArgumentException(
                    "Flight number is required");
        }

        if (flight.getAircraft() == null
                || flight.getAircraft().getId() == null) {

            throw new IllegalArgumentException(
                    "Aircraft is required");
        }

        if (flight.getDepartureAirport() == null
                || flight.getDepartureAirport().getId() == null) {

            throw new IllegalArgumentException(
                    "Departure airport is required");
        }

        if (flight.getArrivalAirport() == null
                || flight.getArrivalAirport().getId() == null) {

            throw new IllegalArgumentException(
                    "Arrival airport is required");
        }

        if (flight.getRoute() == null
                || flight.getRoute().getId() == null) {

            throw new IllegalArgumentException(
                    "Route is required");
        }

        if (flight.getScheduledDeparture() == null) {

            throw new IllegalArgumentException(
                    "Scheduled departure is required");
        }

        if (flight.getScheduledArrival() == null) {

            throw new IllegalArgumentException(
                    "Scheduled arrival is required");
        }

        if (!flight.getScheduledDeparture()
                .isBefore(flight.getScheduledArrival())) {

            throw new IllegalArgumentException(
                    "Scheduled departure must be before scheduled arrival");
        }

        if (flight.getStatus() == null
                || flight.getStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "Flight status is required");
        }
    }
}