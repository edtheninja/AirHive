package com.airhive.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.RouteRequestDTO;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.RouteRepository;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(
                routeRepository,
                airportRepository);
    }

    private RouteRequestDTO validRequest() {

        RouteRequestDTO request = new RouteRequestDTO();

        request.setDepartureAirportId(1L);
        request.setArrivalAirportId(2L);
        request.setDistanceKm(1150.0);
        request.setEstimatedDurationMinutes(130);
        request.setStatus("ACTIVE");

        return request;
    }

    private Airport airport(Long id, String iataCode) {

        Airport airport = new Airport();
        airport.setId(id);
        airport.setIataCode(iataCode);

        return airport;
    }

    @Test
    void getAllRoutes_shouldReturnAllRoutes() {

        Route route1 = new Route();
        Route route2 = new Route();

        when(routeRepository.findAll())
                .thenReturn(List.of(route1, route2));

        List<Route> result =
                routeService.getAllRoutes();

        assertEquals(2, result.size());

        verify(routeRepository).findAll();
    }

    @Test
    void getRouteById_shouldReturnRoute_whenExists() {

        Route route = new Route();

        when(routeRepository.findById(1L))
                .thenReturn(Optional.of(route));

        Route result =
                routeService.getRouteById(1L);

        assertEquals(route, result);

        verify(routeRepository).findById(1L);
    }

    @Test
    void getRouteById_shouldThrowException_whenNotFound() {

        when(routeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> routeService.getRouteById(99L));

        assertEquals(
                "Route not found with id: 99",
                exception.getMessage());

        verify(routeRepository).findById(99L);
    }

    @Test
    void createRoute_shouldSaveRoute_whenRequestIsValid() {

        RouteRequestDTO request = validRequest();

        Airport departureAirport =
                airport(1L, "DEL");

        Airport arrivalAirport =
                airport(2L, "BOM");

        when(routeRepository
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L))
                .thenReturn(false);

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(departureAirport));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.of(arrivalAirport));

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Route result =
                routeService.createRoute(request);

        assertEquals(
                departureAirport,
                result.getDepartureAirport());

        assertEquals(
                arrivalAirport,
                result.getArrivalAirport());

        assertEquals(
                1150.0,
                result.getDistanceKm());

        assertEquals(
                130,
                result.getEstimatedDurationMinutes());

        assertEquals(
                "ACTIVE",
                result.getStatus());

        verify(routeRepository)
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L);

        verify(airportRepository).findById(1L);
        verify(airportRepository).findById(2L);
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void createRoute_shouldThrowException_whenRouteAlreadyExists() {

        RouteRequestDTO request = validRequest();

        when(routeRepository
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Route already exists from airport 1 to airport 2",
                exception.getMessage());

        verify(routeRepository)
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L);

        verify(airportRepository, never())
                .findById(anyLong());

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void createRoute_shouldThrowException_whenDepartureAirportNotFound() {

        RouteRequestDTO request = validRequest();

        when(routeRepository
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L))
                .thenReturn(false);

        when(airportRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Departure airport not found with id: 1",
                exception.getMessage());

        verify(airportRepository).findById(1L);

        verify(airportRepository, never())
                .findById(2L);

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void createRoute_shouldThrowException_whenArrivalAirportNotFound() {

        RouteRequestDTO request = validRequest();

        when(routeRepository
                .existsByDepartureAirportIdAndArrivalAirportId(
                        1L, 2L))
                .thenReturn(false);

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(
                        airport(1L, "DEL")));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Arrival airport not found with id: 2",
                exception.getMessage());

        verify(airportRepository).findById(1L);
        verify(airportRepository).findById(2L);

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void createRoute_shouldThrowException_whenDepartureAirportMissing() {

        RouteRequestDTO request = validRequest();
        request.setDepartureAirportId(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Departure airport is required",
                exception.getMessage());

        verifyNoInteractions(routeRepository, airportRepository);
    }

    @Test
    void createRoute_shouldThrowException_whenAirportsAreSame() {

        RouteRequestDTO request = validRequest();
        request.setArrivalAirportId(1L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Departure and arrival airports cannot be the same",
                exception.getMessage());

        verifyNoInteractions(routeRepository, airportRepository);
    }

    @Test
    void createRoute_shouldThrowException_whenDistanceIsInvalid() {

        RouteRequestDTO request = validRequest();
        request.setDistanceKm(0.0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Distance must be greater than zero",
                exception.getMessage());

        verifyNoInteractions(routeRepository, airportRepository);
    }

    @Test
    void createRoute_shouldThrowException_whenDurationIsInvalid() {

        RouteRequestDTO request = validRequest();
        request.setEstimatedDurationMinutes(0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Estimated duration must be greater than zero",
                exception.getMessage());

        verifyNoInteractions(routeRepository, airportRepository);
    }

    @Test
    void createRoute_shouldThrowException_whenStatusIsMissing() {

        RouteRequestDTO request = validRequest();
        request.setStatus("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> routeService.createRoute(request));

        assertEquals(
                "Route status is required",
                exception.getMessage());

        verifyNoInteractions(routeRepository, airportRepository);
    }

    @Test
    void updateRoute_shouldUpdate_whenRequestIsValid() {

        Airport oldDeparture = airport(1L, "DEL");
        Airport oldArrival = airport(2L, "BOM");

        Route existingRoute = new Route();
        existingRoute.setDepartureAirport(oldDeparture);
        existingRoute.setArrivalAirport(oldArrival);
        existingRoute.setDistanceKm(1000.0);
        existingRoute.setEstimatedDurationMinutes(120);
        existingRoute.setStatus("ACTIVE");

        RouteRequestDTO request = validRequest();

        when(routeRepository.findById(10L))
                .thenReturn(Optional.of(existingRoute));

        when(airportRepository.findById(1L))
                .thenReturn(Optional.of(oldDeparture));

        when(airportRepository.findById(2L))
                .thenReturn(Optional.of(oldArrival));

        when(routeRepository.save(existingRoute))
                .thenReturn(existingRoute);

        Route result =
                routeService.updateRoute(10L, request);

        assertEquals(1150.0, result.getDistanceKm());
        assertEquals(130, result.getEstimatedDurationMinutes());
        assertEquals("ACTIVE", result.getStatus());

        verify(routeRepository).findById(10L);

        verify(airportRepository).findById(1L);
        verify(airportRepository).findById(2L);

        verify(routeRepository).save(existingRoute);

        verify(routeRepository, never())
                .existsByDepartureAirportIdAndArrivalAirportId(
                        anyLong(), anyLong());
    }

    @Test
    void deleteRoute_shouldDelete_whenRouteExists() {

        Route route = new Route();

        when(routeRepository.findById(1L))
                .thenReturn(Optional.of(route));

        routeService.deleteRoute(1L);

        verify(routeRepository).findById(1L);
        verify(routeRepository).delete(route);
    }
}