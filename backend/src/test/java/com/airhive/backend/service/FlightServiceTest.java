package com.airhive.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.airhive.backend.dto.FlightRequestDTO;
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

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private FlightService flightService;

    private Aircraft aircraft;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private Route route;
    private FlightRequestDTO request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        aircraft = new Aircraft();

        departureAirport = new Airport();
        departureAirport.setId(1L);

        arrivalAirport = new Airport();
        arrivalAirport.setId(2L);

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);

        request = new FlightRequestDTO();
        request.setFlightNumber("AI101");
        request.setAircraftId(1L);
        request.setDepartureAirportId(1L);
        request.setArrivalAirportId(2L);
        request.setRouteId(1L);
        request.setScheduledDeparture(
                LocalDateTime.of(2026, 8, 25, 10, 0));
        request.setScheduledArrival(
                LocalDateTime.of(2026, 8, 25, 12, 0));
        request.setStatus("SCHEDULED");
    }

    @Test
    void createFlight_shouldCreateSuccessfully() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.of(aircraft));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(departureAirport));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.of(arrivalAirport));

        when(routeRepository.findById(1L))
                .thenReturn(Optional.of(route));

        when(flightRepository.countAircraftScheduleConflicts(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(0L);

        Flight savedFlight = new Flight();

        when(flightRepository.save(any(Flight.class)))
                .thenReturn(savedFlight);

        Flight result = flightService.createFlight(request);

        assertNotNull(result);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void createFlight_shouldThrowException_whenFlightNumberExists() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(true);

        assertNotNull(assertThrows(
                DuplicateResourceException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void createFlight_shouldThrowException_whenAircraftNotFound() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertNotNull(assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void createFlight_shouldThrowException_whenDepartureAirportNotFound() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.of(aircraft));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertNotNull(assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void createFlight_shouldThrowException_whenArrivalAirportNotFound() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.of(aircraft));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(departureAirport));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertNotNull(assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void createFlight_shouldThrowException_whenRouteNotFound() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.of(aircraft));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(departureAirport));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.of(arrivalAirport));

        when(routeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertNotNull(assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void createFlight_shouldThrowException_whenAircraftScheduleConflicts() {

        when(flightRepository.existsByFlightNumber("AI101"))
                .thenReturn(false);

        when(aircraftRepository.findById(1L))
                .thenReturn(Optional.of(aircraft));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(departureAirport));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.of(arrivalAirport));

        when(routeRepository.findById(1L))
                .thenReturn(Optional.of(route));

        when(flightRepository.countAircraftScheduleConflicts(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(1L);

        assertNotNull(assertThrows(
                DuplicateResourceException.class,
                () -> flightService.createFlight(request)));
    }

    @Test
    void getFlightById_shouldReturnFlight() {

        Flight flight = new Flight();

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        Flight result = flightService.getFlightById(1L);

        assertNotNull(result);
        assertSame(flight, result);
    }

    @Test
    void getFlightById_shouldThrowException_whenNotFound() {

        when(flightRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertNotNull(assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.getFlightById(999L)));
    }

    @Test
    void getAllFlights_shouldReturnFlights() {

        List<Flight> flights = List.of(
                new Flight(),
                new Flight());

        when(flightRepository.findAll())
                .thenReturn(flights);

        List<Flight> result = flightService.getAllFlights();

        assertEquals(2, result.size());
        verify(flightRepository).findAll();
    }
}