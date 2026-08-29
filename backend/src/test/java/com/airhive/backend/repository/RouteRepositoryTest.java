package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;

@DataJpaTest
class RouteRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private AirportRepository airportRepository;

    private Airport departureAirport;
    private Airport arrivalAirport;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        departureAirport = new Airport();
        departureAirport.setIataCode("TST");
        departureAirport.setIcaoCode("VTST");
        departureAirport.setName("Test Departure Airport");
        departureAirport.setCity("Test City");
        departureAirport.setCountry("India");
        departureAirport.setTerminalCount(1);
        departureAirport.setStatus("ACTIVE");

        departureAirport =
                airportRepository.saveAndFlush(departureAirport);

        arrivalAirport = new Airport();
        arrivalAirport.setIataCode("TSA");
        arrivalAirport.setIcaoCode("VTSA");
        arrivalAirport.setName("Test Arrival Airport");
        arrivalAirport.setCity("Test Arrival City");
        arrivalAirport.setCountry("India");
        arrivalAirport.setTerminalCount(1);
        arrivalAirport.setStatus("ACTIVE");

        arrivalAirport =
                airportRepository.saveAndFlush(arrivalAirport);

        Route route = new Route();

        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1000.0);
        route.setEstimatedDurationMinutes(120);
        route.setStatus("ACTIVE");

        routeRepository.saveAndFlush(route);
    }

    @Test
    void findByDepartureAirportIdAndArrivalAirportId_shouldFindRoute() {

        var result =
                routeRepository.findByDepartureAirportIdAndArrivalAirportId(
                        departureAirport.getId(),
                        arrivalAirport.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void findByDepartureAirportIdAndArrivalAirportId_shouldReturnEmpty_whenRouteDoesNotExist() {

        var result =
                routeRepository.findByDepartureAirportIdAndArrivalAirportId(
                        arrivalAirport.getId(),
                        departureAirport.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByDepartureAirportIdAndArrivalAirportId_shouldReturnTrue_whenRouteExists() {

        assertTrue(
                routeRepository.existsByDepartureAirportIdAndArrivalAirportId(
                        departureAirport.getId(),
                        arrivalAirport.getId()));
    }

    @Test
    void existsByDepartureAirportIdAndArrivalAirportId_shouldReturnFalse_whenRouteDoesNotExist() {

        assertFalse(
                routeRepository.existsByDepartureAirportIdAndArrivalAirportId(
                        arrivalAirport.getId(),
                        departureAirport.getId()));
    }
}