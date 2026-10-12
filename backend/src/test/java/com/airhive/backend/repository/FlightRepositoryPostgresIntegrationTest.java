package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.entity.Route;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class FlightRepositoryPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6");

    static {
        POSTGRES.start();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

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
        flightRepository.deleteAllInBatch();
        routeRepository.deleteAllInBatch();
        aircraftRepository.deleteAllInBatch();
        aircraftTypeRepository.deleteAllInBatch();
        airportRepository.deleteAllInBatch();

        AircraftType aircraftType = new AircraftType();
        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);
        aircraftType = aircraftTypeRepository.saveAndFlush(aircraftType);

        aircraft = new Aircraft();
        aircraft.setRegistrationNumber("VT-TEST01");
        aircraft.setAircraftType(aircraftType);
        aircraft.setStatus("ACTIVE");
        aircraft = aircraftRepository.saveAndFlush(aircraft);

        departureAirport = new Airport();
        departureAirport.setIataCode("TST");
        departureAirport.setIcaoCode("VTST");
        departureAirport.setName("Test Departure Airport");
        departureAirport.setCity("Test City");
        departureAirport.setCountry("India");
        departureAirport.setTerminalCount(1);
        departureAirport.setStatus("ACTIVE");
        departureAirport = airportRepository.saveAndFlush(departureAirport);

        arrivalAirport = new Airport();
        arrivalAirport.setIataCode("TSA");
        arrivalAirport.setIcaoCode("VTSA");
        arrivalAirport.setName("Test Arrival Airport");
        arrivalAirport.setCity("Test Arrival City");
        arrivalAirport.setCountry("India");
        arrivalAirport.setTerminalCount(1);
        arrivalAirport.setStatus("ACTIVE");
        arrivalAirport = airportRepository.saveAndFlush(arrivalAirport);

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1000.0);
        route.setEstimatedDurationMinutes(120);
        route.setStatus("ACTIVE");
        route = routeRepository.saveAndFlush(route);
    }

    @Test
    void countAircraftScheduleConflicts_detectsOverlappingFlight() {
        saveFlight(
                "PG101",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0)
        );

        long conflicts = flightRepository.countAircraftScheduleConflicts(
                aircraft.getId(),
                LocalDateTime.of(2026, 8, 29, 11, 0),
                LocalDateTime.of(2026, 8, 29, 9, 0)
        );

        assertEquals(1, conflicts);
    }

    @Test
    void countAircraftScheduleConflicts_allowsAdjacentFlights() {
        saveFlight(
                "PG102",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0)
        );

        long conflicts = flightRepository.countAircraftScheduleConflicts(
                aircraft.getId(),
                LocalDateTime.of(2026, 8, 29, 14, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0)
        );

        assertEquals(0, conflicts);
    }

    @Test
    void countAircraftScheduleConflictsForUpdate_ignoresSameFlight() {
        Flight flight = saveFlight(
                "PG103",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0)
        );

        long conflicts =
                flightRepository.countAircraftScheduleConflictsForUpdate(
                        aircraft.getId(),
                        flight.getId(),
                        LocalDateTime.of(2026, 8, 29, 12, 0),
                        LocalDateTime.of(2026, 8, 29, 10, 0)
                );

        assertEquals(0, conflicts);
    }

    private Flight saveFlight(
            String flightNumber,
            LocalDateTime departure,
            LocalDateTime arrival
    ) {
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

