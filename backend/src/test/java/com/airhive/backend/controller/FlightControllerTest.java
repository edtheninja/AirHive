package com.airhive.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.service.FlightService;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    private Flight flight;
    private Aircraft aircraft;
    private Route route;
    private Airport departureAirport;
    private Airport arrivalAirport;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        departureAirport = new Airport();
        departureAirport.setIataCode("DEL");

        arrivalAirport = new Airport();
        arrivalAirport.setIataCode("BOM");

        aircraft = new Aircraft();
        aircraft.setRegistrationNumber("VT-AIR01");

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1150.0);
        route.setEstimatedDurationMinutes(135);
        route.setStatus("ACTIVE");

        flight = new Flight();
        flight.setFlightNumber("AI101");
        flight.setAircraft(aircraft);
        flight.setRoute(route);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setScheduledDeparture(
                LocalDateTime.of(2026, 8, 27, 10, 0));
        flight.setScheduledArrival(
                LocalDateTime.of(2026, 8, 27, 12, 15));
        flight.setStatus("SCHEDULED");
    }

    @Test
    void getAllFlights_shouldReturn200() throws Exception {

        when(flightService.getAllFlights())
                .thenReturn(List.of(flight));

        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber")
                        .value("AI101"))
                .andExpect(jsonPath("$[0].status")
                        .value("SCHEDULED"))
                .andExpect(jsonPath("$[0].aircraftRegistration")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$[0].departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$[0].arrivalAirportCode")
                        .value("BOM"));
    }

    @Test
    void getFlightById_shouldReturn200() throws Exception {

        when(flightService.getFlightById(1L))
                .thenReturn(flight);

        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber")
                        .value("AI101"))
                .andExpect(jsonPath("$.status")
                        .value("SCHEDULED"))
                .andExpect(jsonPath("$.aircraftRegistration")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$.departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.arrivalAirportCode")
                        .value("BOM"));
    }

    @Test
    void getFlightById_shouldReturn404_whenNotFound()
            throws Exception {

        when(flightService.getFlightById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Flight not found with id: 999"));

        mockMvc.perform(get("/api/flights/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Flight not found with id: 999"));
    }

    @Test
    void createFlight_shouldReturn201() throws Exception {

        when(flightService.createFlight(any(
                com.airhive.backend.dto.FlightRequestDTO.class)))
                .thenReturn(flight);

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber")
                        .value("AI101"))
                .andExpect(jsonPath("$.status")
                        .value("SCHEDULED"))
                .andExpect(jsonPath("$.aircraftRegistration")
                        .value("VT-AIR01"));
    }

    @Test
    void createFlight_shouldReturn400_whenFlightNumberMissing()
            throws Exception {

        String request = """
                {
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_shouldReturn400_whenAircraftIdMissing()
            throws Exception {

        String request = """
                {
                    "flightNumber": "AI101",
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_shouldReturn400_whenRouteIdMissing()
            throws Exception {

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_shouldReturn400_whenAircraftIdNotPositive()
            throws Exception {

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 0,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_shouldReturn400_whenStatusMissing()
            throws Exception {

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_shouldReturn409_whenDuplicate()
            throws Exception {

        when(flightService.createFlight(any(
                com.airhive.backend.dto.FlightRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Flight already exists with flight number: AI101"));

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409));
    }

    @Test
    void updateFlight_shouldReturn200() throws Exception {

        when(flightService.updateFlight(
                eq(1L),
                any(com.airhive.backend.dto.FlightRequestDTO.class)))
                .thenReturn(flight);

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(put("/api/flights/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber")
                        .value("AI101"))
                .andExpect(jsonPath("$.status")
                        .value("SCHEDULED"));
    }

    @Test
    void updateFlight_shouldReturn400_whenInvalidRequest()
            throws Exception {

        String request = """
                {
                    "flightNumber": "",
                    "aircraftId": null,
                    "routeId": null,
                    "departureAirportId": null,
                    "arrivalAirportId": null,
                    "scheduledDeparture": null,
                    "scheduledArrival": null,
                    "status": ""
                }
                """;

        mockMvc.perform(put("/api/flights/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFlight_shouldReturn404_whenNotFound()
            throws Exception {

        when(flightService.updateFlight(
                eq(999L),
                any(com.airhive.backend.dto.FlightRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Flight not found with id: 999"));

        String request = """
                {
                    "flightNumber": "AI101",
                    "aircraftId": 1,
                    "routeId": 1,
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "scheduledDeparture": "2026-08-27T10:00:00",
                    "scheduledArrival": "2026-08-27T12:15:00",
                    "status": "SCHEDULED"
                }
                """;

        mockMvc.perform(put("/api/flights/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFlight_shouldReturn204() throws Exception {

        doNothing().when(flightService).deleteFlight(1L);

        mockMvc.perform(delete("/api/flights/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteFlight_shouldReturn404_whenNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Flight not found with id: 999"))
                .when(flightService)
                .deleteFlight(999L);

        mockMvc.perform(delete("/api/flights/999"))
                .andExpect(status().isNotFound());
    }
}

