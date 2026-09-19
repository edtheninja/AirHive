package com.airhive.backend.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.airhive.backend.dto.FlightRequestDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.dto.FlightStatusUpdateRequestDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightActivityRepository;
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

        @Mock
        private FlightWebSocketService flightWebSocketService;

        @Mock
        private FlightNotificationService flightNotificationService;

        @Mock
        private FlightActivityService flightActivityService;

        @Mock
        private FlightActivityRepository flightActivityRepository;

        @InjectMocks
        private FlightService flightService;

        private Aircraft aircraft;
        private Airport departureAirport;
        private Airport arrivalAirport;
        private Route route;
        private FlightRequestDTO request;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                MockitoAnnotations.openMocks(this);

                aircraft = new Aircraft();
                setId(aircraft, 1L);
                aircraft.setRegistrationNumber("VT-AIR01");

                departureAirport = new Airport();
                departureAirport.setId(1L);
                departureAirport.setIataCode("DEL");

                arrivalAirport = new Airport();
                arrivalAirport.setId(2L);
                arrivalAirport.setIataCode("BOM");

                route = new Route();
                setId(route, 1L);
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
                setId(savedFlight, 1L);
                savedFlight.setFlightNumber("AI101");
                savedFlight.setAircraft(aircraft);
                savedFlight.setRoute(route);
                savedFlight.setDepartureAirport(departureAirport);
                savedFlight.setArrivalAirport(arrivalAirport);
                savedFlight.setScheduledDeparture(
                                request.getScheduledDeparture());
                savedFlight.setScheduledArrival(
                                request.getScheduledArrival());
                savedFlight.setStatus("SCHEDULED");

                when(flightRepository.save(any(Flight.class)))
                                .thenReturn(savedFlight);

                FlightResponseDTO result = flightService.createFlight(request);

                assertNotNull(result);
                assertEquals("AI101", result.getFlightNumber());
                assertEquals(1L, result.getAircraftId());
                assertEquals(1L, result.getRouteId());
                assertEquals("DEL", result.getDepartureAirportCode());
                assertEquals("BOM", result.getArrivalAirportCode());

                verify(flightRepository).save(any(Flight.class));
                verify(flightWebSocketService)
                                .publishFlightCreated(any(FlightResponseDTO.class));
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

                Flight flight = createFlightFixture();

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(flight));

                FlightResponseDTO result = flightService.getFlightById(1L);

                assertNotNull(result);
                assertEquals(1L, result.getId());
                assertEquals("AI101", result.getFlightNumber());
                assertEquals(1L, result.getAircraftId());
                assertEquals("VT-AIR01", result.getAircraftRegistration());
                assertEquals(1L, result.getRouteId());
                assertEquals(1L, result.getDepartureAirportId());
                assertEquals("DEL", result.getDepartureAirportCode());
                assertEquals(2L, result.getArrivalAirportId());
                assertEquals("BOM", result.getArrivalAirportCode());
                assertEquals("SCHEDULED", result.getStatus());
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

                Flight flight1 = createFlightFixture();
                setId(flight1, 1L);
                flight1.setFlightNumber("AI101");

                Flight flight2 = createFlightFixture();
                setId(flight2, 2L);
                flight2.setFlightNumber("AI102");

                List<Flight> flights = List.of(
                                flight1,
                                flight2);

                when(flightRepository.findAll())
                                .thenReturn(flights);

                List<FlightResponseDTO> result = flightService.getAllFlights();

                assertEquals(2, result.size());
                assertEquals("AI101", result.get(0).getFlightNumber());
                assertEquals("AI102", result.get(1).getFlightNumber());

                verify(flightRepository).findAll();
        }

        @Test
        void updateFlight_shouldUpdateSuccessfully() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

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

                when(flightRepository.countAircraftScheduleConflictsForUpdate(
                                eq(1L),
                                eq(1L),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class)))
                                .thenReturn(0L);

                when(flightRepository.save(existingFlight))
                                .thenReturn(existingFlight);

                FlightResponseDTO result = flightService.updateFlight(1L, request);

                assertNotNull(result);
                assertEquals("AI101", result.getFlightNumber());
                assertEquals("SCHEDULED", result.getStatus());
                assertEquals(1L, result.getAircraftId());
                assertEquals(1L, result.getRouteId());

                verify(flightRepository).save(existingFlight);
                verify(flightWebSocketService)
                                .publishFlightUpdated(any(FlightResponseDTO.class));
        }

        @Test
        void updateFlight_shouldThrowException_whenFlightNumberAlreadyExists() {

                Flight existingFlight = new Flight();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                when(flightRepository.existsByFlightNumber("AI101"))
                                .thenReturn(true);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlight_shouldThrowException_whenAircraftNotFound() {

                Flight existingFlight = new Flight();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                when(flightRepository.existsByFlightNumber("AI101"))
                                .thenReturn(false);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlight_shouldThrowException_whenDepartureAirportNotFound() {

                Flight existingFlight = new Flight();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                when(flightRepository.existsByFlightNumber("AI101"))
                                .thenReturn(false);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(aircraft));

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlight_shouldThrowException_whenArrivalAirportNotFound() {

                Flight existingFlight = new Flight();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                when(flightRepository.existsByFlightNumber("AI101"))
                                .thenReturn(false);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(aircraft));

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.of(departureAirport));

                when(airportRepository.findById(2L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlight_shouldThrowException_whenRouteNotFound() {

                Flight existingFlight = new Flight();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

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

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlight_shouldThrowException_whenAircraftScheduleConflicts() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setFlightNumber("AI100");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

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

                when(flightRepository.countAircraftScheduleConflictsForUpdate(
                                eq(1L),
                                eq(1L),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class)))
                                .thenReturn(1L);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> flightService.updateFlight(1L, request));

                assertNotNull(exception);
        }

        @Test
        void updateFlightStatus_shouldAllowValidTransition() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setStatus("SCHEDULED");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));
                when(flightRepository.save(existingFlight))
                                .thenReturn(existingFlight);

                FlightResponseDTO response = flightService.updateFlightStatus(
                                1L,
                                createStatusRequest("BOARDING", null));

                assertNotNull(response);
                assertEquals("BOARDING", existingFlight.getStatus());

                verify(flightRepository).save(existingFlight);
                verify(flightNotificationService)
                                .notifyFlightStatusChange(
                                                any(FlightResponseDTO.class),
                                                eq("SCHEDULED"));
                verify(flightActivityService)
                                .recordStatusChange(
                                                existingFlight,
                                                "SCHEDULED",
                                                "BOARDING",
                                                null);
                verify(flightWebSocketService)
                                .publishFlightUpdated(any(FlightResponseDTO.class));
        }

        @Test
        void updateFlightStatus_shouldRejectInvalidTransition() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setStatus("SCHEDULED");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> flightService.updateFlightStatus(
                                                1L,
                                                createStatusRequest("LANDED", null)));

                assertEquals(
                                "Invalid flight status transition from SCHEDULED to LANDED",
                                exception.getMessage());

                verify(flightRepository, never()).save(any(Flight.class));
                verifyNoInteractions(
                                flightNotificationService,
                                flightActivityService,
                                flightWebSocketService);
        }

        @Test
        void updateFlightStatus_shouldAllowCancellationBeforeDeparture() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setStatus("BOARDING");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));
                when(flightRepository.save(existingFlight))
                                .thenReturn(existingFlight);

                FlightResponseDTO response = flightService.updateFlightStatus(
                                1L,
                                createStatusRequest(
                                                "CANCELLED",
                                                "Operational cancellation"));

                assertNotNull(response);
                assertEquals("CANCELLED", existingFlight.getStatus());

                verify(flightRepository).save(existingFlight);
        }

        @Test
        void updateFlightStatus_shouldRejectCancellationAfterDeparture() {

                Flight existingFlight = createFlightFixture();
                existingFlight.setStatus("DEPARTED");

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> flightService.updateFlightStatus(
                                                1L,
                                                createStatusRequest(
                                                                "CANCELLED",
                                                                "Operational cancellation")));

                assertEquals(
                                "Invalid flight status transition from DEPARTED to CANCELLED",
                                exception.getMessage());

                verify(flightRepository, never()).save(any(Flight.class));
                verifyNoInteractions(
                                flightNotificationService,
                                flightActivityService,
                                flightWebSocketService);
        }

        @Test
        void deleteFlight_shouldDeleteSuccessfully() {

                Flight existingFlight = createFlightFixture();

                when(flightRepository.findById(1L))
                                .thenReturn(Optional.of(existingFlight));

                when(flightActivityRepository.existsByFlightId(1L))
                                .thenReturn(false);

                flightService.deleteFlight(1L);

                verify(flightRepository).delete(existingFlight);
                verify(flightWebSocketService)
                                .publishFlightDeleted(any(FlightResponseDTO.class));
        }

        @Test
        void deleteFlight_shouldThrowException_whenNotFound() {

                when(flightRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertNotNull(assertThrows(
                                ResourceNotFoundException.class,
                                () -> flightService.deleteFlight(999L)));
        }

        private FlightStatusUpdateRequestDTO createStatusRequest(
                        String status,
                        String reason) {

                FlightStatusUpdateRequestDTO request = new FlightStatusUpdateRequestDTO();

                request.setStatus(status);
                request.setReason(reason);

                return request;
        }

        private Flight createFlightFixture() {

                Flight flight = new Flight();

                setId(flight, 1L);
                flight.setFlightNumber("AI101");
                flight.setAircraft(aircraft);
                flight.setRoute(route);
                flight.setDepartureAirport(departureAirport);
                flight.setArrivalAirport(arrivalAirport);
                flight.setScheduledDeparture(
                                request.getScheduledDeparture());
                flight.setScheduledArrival(
                                request.getScheduledArrival());
                flight.setStatus("SCHEDULED");

                return flight;
        }

        @SuppressWarnings("UseSpecificCatch")
        private void setId(Object entity, long id) {
                try {
                        Field f = entity.getClass().getDeclaredField("id");
                        f.setAccessible(true);
                        f.set(entity, id);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }
}
