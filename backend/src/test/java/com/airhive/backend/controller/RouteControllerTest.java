package com.airhive.backend.controller;

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

import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.service.RouteService;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteService routeService;

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

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1150.0);
        route.setEstimatedDurationMinutes(130);
        route.setStatus("ACTIVE");
    }

    @Test
    void getAllRoutes_shouldReturn200() throws Exception {

        when(routeService.getAllRoutes())
                .thenReturn(List.of(route));

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$[0].arrivalAirportCode")
                        .value("BOM"))
                .andExpect(jsonPath("$[0].distanceKm")
                        .value(1150.0))
                .andExpect(jsonPath("$[0].estimatedDurationMinutes")
                        .value(130))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"));
    }

    @Test
    void getRouteById_shouldReturn200() throws Exception {

        when(routeService.getRouteById(1L))
                .thenReturn(route);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.arrivalAirportCode")
                        .value("BOM"))
                .andExpect(jsonPath("$.distanceKm")
                        .value(1150.0))
                .andExpect(jsonPath("$.estimatedDurationMinutes")
                        .value(130))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void getRouteById_shouldReturn404_whenNotFound()
            throws Exception {

        when(routeService.getRouteById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Route not found with id: 999"));

        mockMvc.perform(get("/api/routes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Route not found with id: 999"));
    }

    @Test
    void createRoute_shouldReturn201() throws Exception {

        when(routeService.createRoute(any(
                com.airhive.backend.dto.RouteRequestDTO.class)))
                .thenReturn(route);

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.arrivalAirportCode")
                        .value("BOM"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void createRoute_shouldReturn400_whenDepartureAirportIdMissing()
            throws Exception {

        String request = """
                {
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenArrivalAirportIdMissing()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenAirportIdNotPositive()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 0,
                    "arrivalAirportId": -1,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenDistanceMissing()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenDistanceNotPositive()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenDurationMissing()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenDurationNotPositive()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 0,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn400_whenStatusMissing()
            throws Exception {

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoute_shouldReturn409_whenDuplicate()
            throws Exception {

        when(routeService.createRoute(any(
                com.airhive.backend.dto.RouteRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Route already exists"));

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409));
    }

    @Test
    void updateRoute_shouldReturn200() throws Exception {

        when(routeService.updateRoute(
                eq(1L),
                any(com.airhive.backend.dto.RouteRequestDTO.class)))
                .thenReturn(route);

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departureAirportCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.arrivalAirportCode")
                        .value("BOM"));
    }

    @Test
    void updateRoute_shouldReturn400_whenInvalidRequest()
            throws Exception {

        String request = """
                {
                    "departureAirportId": null,
                    "arrivalAirportId": 0,
                    "distanceKm": 0,
                    "estimatedDurationMinutes": -1,
                    "status": ""
                }
                """;

        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRoute_shouldReturn404_whenNotFound()
            throws Exception {

        when(routeService.updateRoute(
                eq(999L),
                any(com.airhive.backend.dto.RouteRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Route not found with id: 999"));

        String request = """
                {
                    "departureAirportId": 1,
                    "arrivalAirportId": 2,
                    "distanceKm": 1150.0,
                    "estimatedDurationMinutes": 130,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/routes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404));
    }

    @Test
    void deleteRoute_shouldReturn204() throws Exception {

        doNothing().when(routeService).deleteRoute(1L);

        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRoute_shouldReturn404_whenNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Route not found with id: 999"))
                .when(routeService)
                .deleteRoute(999L);

        mockMvc.perform(delete("/api/routes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404));
    }
}

