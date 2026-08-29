package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Aircraft aircraft;
    private Route route;
    private Airport departureAirport;
    private Airport arrivalAirport;

    @BeforeEach
    void setUp() {

        AircraftType aircraftType = new AircraftType();
        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);

        aircraftType =
                aircraftTypeRepository.saveAndFlush(aircraftType);

        aircraft = new Aircraft();
        aircraft.setRegistrationNumber("VT-TEST01");
        aircraft.setAircraftType(aircraftType);
        aircraft.setStatus("ACTIVE");

        aircraft =
                aircraftRepository.saveAndFlush(aircraft);

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

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1000.0);
        route.setEstimatedDurationMinutes(120);
        route.setStatus("ACTIVE");

        route =
                routeRepository.saveAndFlush(route);
    }

    @Test
    void countAircraftScheduleConflicts_shouldReturnZero_whenNoConflict() {

        LocalDateTime departure =
                LocalDateTime.of(2026, 8, 29, 10, 0);

        LocalDateTime arrival =
                LocalDateTime.of(2026, 8, 29, 12, 0);

        long conflicts =
                flightRepository.countAircraftScheduleConflicts(
                        aircraft.getId(),
                        arrival,
                        departure);

        assertEquals(0, conflicts);
    }

    @Test
    void countAircraftScheduleConflicts_shouldDetectOverlap() {

        saveFlight(
                "AH101",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));

        long conflicts =
                flightRepository.countAircraftScheduleConflicts(
                        aircraft.getId(),
                        LocalDateTime.of(2026, 8, 29, 11, 0),
                        LocalDateTime.of(2026, 8, 29, 9, 0));

        assertEquals(1, conflicts);
    }

    @Test
    void countAircraftScheduleConflicts_shouldNotDetectAdjacentFlights() {

        saveFlight(
                "AH102",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));

        long conflicts =
                flightRepository.countAircraftScheduleConflicts(
                        aircraft.getId(),
                        LocalDateTime.of(2026, 8, 29, 14, 0),
                        LocalDateTime.of(2026, 8, 29, 12, 0));

        assertEquals(0, conflicts);
    }

    @Test
    void countAircraftScheduleConflictsForUpdate_shouldIgnoreSameFlight() {

        Flight flight = saveFlight(
                "AH103",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));

        long conflicts =
                flightRepository.countAircraftScheduleConflictsForUpdate(
                        aircraft.getId(),
                        flight.getId(),
                        LocalDateTime.of(2026, 8, 29, 12, 0),
                        LocalDateTime.of(2026, 8, 29, 10, 0));

        assertEquals(0, conflicts);
    }

    @Test
    void countAircraftScheduleConflictsForUpdate_shouldDetectOtherFlight() {

        saveFlight(
                "AH104",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));

        Flight updatingFlight = saveFlight(
                "AH105",
                LocalDateTime.of(2026, 8, 29, 15, 0),
                LocalDateTime.of(2026, 8, 29, 17, 0));

        long conflicts =
                flightRepository.countAircraftScheduleConflictsForUpdate(
                        aircraft.getId(),
                        updatingFlight.getId(),
                        LocalDateTime.of(2026, 8, 29, 11, 0),
                        LocalDateTime.of(2026, 8, 29, 9, 0));

        assertEquals(1, conflicts);
    }

    private Flight saveFlight(
            String flightNumber,
            LocalDateTime departure,
            LocalDateTime arrival) {

        Flight flight = new Flight();

        flight.setFlightNumber(flightNumber);
        flight.setAircraft(aircraft);
        flight.setRoute(route);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setScheduledDeparture(departure);
        flight.setScheduledArrival(arrival);
        flight.setStatus("SCHEDULED");

        return flightRepository.saveAndFlush(flight);
    }
}

