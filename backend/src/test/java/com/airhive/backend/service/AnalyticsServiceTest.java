package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.ActivityCountDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.AircraftUtilizationDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.HistoricalActivityDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.StatusCountDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.FlightRepository;

class AnalyticsServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOverviewReturnsZeroCountsAndEmptyListsWhenNoDataExists() {
        when(flightRepository.findAll()).thenReturn(List.of());
        when(aircraftRepository.findAll()).thenReturn(List.of());

        AnalyticsOverviewResponseDTO result = analyticsService.getOverview();

        assertNotNull(result);
        assertEquals(0, result.getTotalFlights());
        assertEquals(0, result.getDelayedFlights());
        assertEquals(0, result.getCancelledFlights());
        assertEquals(0, result.getAirborneFlights());

        assertEquals(0, result.getTotalAircraft());
        assertEquals(0, result.getActiveAircraft());
        assertEquals(0, result.getInactiveAircraft());
        assertEquals(0, result.getMaintenanceAircraft());

        assertEquals(List.of(), result.getFlightStatusDistribution());
        assertEquals(List.of(), result.getAircraftUtilization());
        assertEquals(List.of(), result.getAirportActivity());
        assertEquals(List.of(), result.getRouteActivity());
        assertEquals(List.of(), result.getHistoricalActivity());

        verify(flightRepository).findAll();
        verify(aircraftRepository).findAll();
    }

    @Test
    void getOverviewCalculatesFlightAndAircraftStatusCounts() throws Exception {
        Aircraft active = aircraft(1L, "VT-AIR01", "ACTIVE", "A320");
        Aircraft inactive = aircraft(2L, "VT-AIR02", "INACTIVE", "B737");
        Aircraft maintenance = aircraft(3L, "VT-AIR03", "MAINTENANCE", "A320");

        Flight delayed = flight(
                "DELAYED",
                active,
                null,
                null,
                LocalDateTime.of(2026, 10, 10, 9, 0));

        Flight cancelled = flight(
                "CANCELLED",
                inactive,
                null,
                null,
                LocalDateTime.of(2026, 10, 10, 10, 0));

        Flight airborne = flight(
                "IN AIR",
                null,
                null,
                null,
                LocalDateTime.of(2026, 10, 10, 11, 0));

        Flight scheduled = flight(
                "SCHEDULED",
                null,
                null,
                null,
                LocalDateTime.of(2026, 10, 10, 12, 0));

        Flight lowercaseDelayed = flight(
                "delayed",
                null,
                null,
                null,
                LocalDateTime.of(2026, 10, 10, 13, 0));

        when(flightRepository.findAll()).thenReturn(
                List.of(delayed, cancelled, airborne, scheduled, lowercaseDelayed));
        when(aircraftRepository.findAll()).thenReturn(
                List.of(active, inactive, maintenance));

        AnalyticsOverviewResponseDTO result = analyticsService.getOverview();

        assertEquals(5, result.getTotalFlights());
        assertEquals(2, result.getDelayedFlights());
        assertEquals(1, result.getCancelledFlights());
        assertEquals(1, result.getAirborneFlights());

        assertEquals(3, result.getTotalAircraft());
        assertEquals(1, result.getActiveAircraft());
        assertEquals(1, result.getInactiveAircraft());
        assertEquals(1, result.getMaintenanceAircraft());

        // Distribution groups by the exact status string, unlike headline counts.
        List<StatusCountDTO> distribution = result.getFlightStatusDistribution();
        assertEquals(5, distribution.size());
        assertEquals("DELAYED", distribution.get(0).getStatus());
        assertEquals(1, distribution.get(0).getCount());
        assertEquals("delayed", distribution.get(4).getStatus());
        assertEquals(1, distribution.get(4).getCount());
    }

    @Test
    void getOverviewBuildsAircraftAirportAndRouteActivity() throws Exception {
        Airport del = airport("DEL");
        Airport bom = airport("BOM");
        Airport blr = airport("BLR");

        AircraftType type = new AircraftType();
        type.setTypeCode("A320");

        Aircraft heavilyUsed = aircraft(1L, "VT-AIR01", "ACTIVE", "A320");
        Aircraft lightlyUsed = aircraft(2L, "VT-AIR02", "ACTIVE", "B737");
        Aircraft unused = aircraft(3L, "VT-AIR03", "MAINTENANCE", null);

        Route delBom = route(del, bom);
        Route delBlr = route(del, blr);

        Flight first = flight(
                "SCHEDULED", heavilyUsed, delBom, del, bom,
                LocalDateTime.of(2026, 10, 10, 9, 0));
        Flight second = flight(
                "DELAYED", heavilyUsed, delBom, del, bom,
                LocalDateTime.of(2026, 10, 11, 9, 0));
        Flight third = flight(
                "CANCELLED", lightlyUsed, delBlr, del, blr,
                LocalDateTime.of(2026, 10, 11, 10, 0));

        when(flightRepository.findAll()).thenReturn(List.of(first, second, third));
        when(aircraftRepository.findAll()).thenReturn(
                List.of(heavilyUsed, lightlyUsed, unused));

        AnalyticsOverviewResponseDTO result = analyticsService.getOverview();

        List<AircraftUtilizationDTO> utilization = result.getAircraftUtilization();
        assertEquals(3, utilization.size());

        assertEquals("VT-AIR01", utilization.get(0).getRegistrationNumber());
        assertEquals(2, utilization.get(0).getAssignedFlights());
        assertEquals("A320", utilization.get(0).getAircraftTypeCode());

        assertEquals("VT-AIR02", utilization.get(1).getRegistrationNumber());
        assertEquals(1, utilization.get(1).getAssignedFlights());

        assertEquals("VT-AIR03", utilization.get(2).getRegistrationNumber());
        assertEquals(0, utilization.get(2).getAssignedFlights());
        assertEquals(null, utilization.get(2).getAircraftTypeCode());

        List<ActivityCountDTO> airports = result.getAirportActivity();
        assertEquals(3, airports.size());

        ActivityCountDTO delActivity = airports.stream()
                .filter(item -> "DEL".equals(item.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(3, delActivity.getDepartures());
        assertEquals(0, delActivity.getArrivals());
        assertEquals(3, delActivity.getTotal());

        ActivityCountDTO bomActivity = airports.stream()
                .filter(item -> "BOM".equals(item.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, bomActivity.getDepartures());
        assertEquals(2, bomActivity.getArrivals());
        assertEquals(2, bomActivity.getTotal());

        List<ActivityCountDTO> routes = result.getRouteActivity();
        assertEquals(2, routes.size());
        assertEquals("DEL → BOM", routes.get(0).getName());
        assertEquals(2, routes.get(0).getTotal());
        assertEquals(0, routes.get(0).getDepartures());
        assertEquals(0, routes.get(0).getArrivals());

        assertEquals("DEL → BLR", routes.get(1).getName());
        assertEquals(1, routes.get(1).getTotal());
    }

    @Test
    void getOverviewBuildsHistoricalActivityInDateOrder() {
        Flight laterDelayed = flight(
                "DELAYED", null, null, null,
                LocalDateTime.of(2026, 10, 12, 9, 0));

        Flight earlierCancelled = flight(
                "CANCELLED", null, null, null,
                LocalDateTime.of(2026, 10, 10, 10, 0));

        Flight sameDayScheduled = flight(
                "SCHEDULED", null, null, null,
                LocalDateTime.of(2026, 10, 12, 15, 0));

        Flight withoutDepartureTime = flight(
                "DELAYED", null, null, null, null);

        when(flightRepository.findAll()).thenReturn(List.of(
                laterDelayed,
                earlierCancelled,
                sameDayScheduled,
                withoutDepartureTime));
        when(aircraftRepository.findAll()).thenReturn(List.of());

        AnalyticsOverviewResponseDTO result = analyticsService.getOverview();

        List<HistoricalActivityDTO> history = result.getHistoricalActivity();
        assertEquals(2, history.size());

        assertEquals("2026-10-10", history.get(0).getDate());
        assertEquals(1, history.get(0).getTotal());
        assertEquals(0, history.get(0).getDelayed());
        assertEquals(1, history.get(0).getCancelled());

        assertEquals("2026-10-12", history.get(1).getDate());
        assertEquals(2, history.get(1).getTotal());
        assertEquals(1, history.get(1).getDelayed());
        assertEquals(0, history.get(1).getCancelled());
    }

    @Test
    void getOverviewHandlesFlightsWithoutOptionalRelationships() {
        Flight flightWithoutRelationships = flight(
                "SCHEDULED", null, null, null, null);

        when(flightRepository.findAll()).thenReturn(
                List.of(flightWithoutRelationships));
        when(aircraftRepository.findAll()).thenReturn(List.of());

        AnalyticsOverviewResponseDTO result = analyticsService.getOverview();

        assertEquals(1, result.getTotalFlights());
        assertEquals(0, result.getAircraftUtilization().size());
        assertEquals(0, result.getAirportActivity().size());
        assertEquals(0, result.getRouteActivity().size());
        assertEquals(0, result.getHistoricalActivity().size());
    }

    private static Flight flight(
            String status,
            Aircraft aircraft,
            Route route,
            Airport departure,
            Airport arrival,
            LocalDateTime scheduledDeparture) {

        Flight flight = new Flight();
        flight.setStatus(status);
        flight.setAircraft(aircraft);
        flight.setRoute(route);
        flight.setDepartureAirport(departure);
        flight.setArrivalAirport(arrival);
        flight.setScheduledDeparture(scheduledDeparture);
        return flight;
    }

    private static Flight flight(
            String status,
            Aircraft aircraft,
            Route route,
            Airport unused,
            LocalDateTime scheduledDeparture) {

        return flight(status, aircraft, route, null, null, scheduledDeparture);
    }

    private static Aircraft aircraft(
            Long id,
            String registration,
            String status,
            String typeCode) throws Exception {

        Aircraft aircraft = new Aircraft();
        setField(aircraft, "id", id);
        aircraft.setRegistrationNumber(registration);
        aircraft.setStatus(status);

        if (typeCode != null) {
            AircraftType type = new AircraftType();
            type.setTypeCode(typeCode);
            aircraft.setAircraftType(type);
        }

        return aircraft;
    }

    private static Airport airport(String iataCode) {
        Airport airport = new Airport();
        airport.setIataCode(iataCode);
        return airport;
    }

    private static Route route(Airport departure, Airport arrival) {
        Route route = new Route();
        route.setDepartureAirport(departure);
        route.setArrivalAirport(arrival);
        return route;
    }

    private static void setField(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}