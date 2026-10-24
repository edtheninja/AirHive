
package com.airhive.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AircraftTypeRepository;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightActivityRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;


import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FlightControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18.6");

    static {
        POSTGRES.start();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword())
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
                () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightActivityRepository flightActivityRepository;

    private Aircraft aircraft;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private Route route;

    @BeforeEach
    void setUp() {
        // Delete in foreign-key dependency order.
        flightActivityRepository.deleteAllInBatch();
        flightRepository.deleteAllInBatch();
        routeRepository.deleteAllInBatch();
        aircraftRepository.deleteAllInBatch();
        airportRepository.deleteAllInBatch();
        aircraftTypeRepository.deleteAllInBatch();

        AircraftType aircraftType = new AircraftType();
        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);

        aircraftType = aircraftTypeRepository.save(aircraftType);

        aircraft = new Aircraft();
        aircraft.setRegistrationNumber("VT-TEST01");
        aircraft.setAircraftType(aircraftType);
        aircraft.setStatus("ACTIVE");

        aircraft = aircraftRepository.save(aircraft);

        departureAirport = createAirport(
                "DEL", "VIDP", "Indira Gandhi International Airport",
                "Delhi", "India");

        arrivalAirport = createAirport(
                "BOM", "VABB", "Chhatrapati Shivaji Maharaj International Airport",
                "Mumbai", "India");

        route = new Route();
        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);
        route.setDistanceKm(1148.0);
        route.setEstimatedDurationMinutes(135);
        route.setStatus("ACTIVE");

        route = routeRepository.save(route);
    }

    @Test
    void getAllFlights_shouldReturnEmptyListWhenNoFlightsExist()
            throws Exception {

        mockMvc.perform(get("/api/flights")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createFlight_shouldPersistFlightAndAllowRetrieval()
            throws Exception {

        LocalDateTime departure = LocalDateTime.of(2027, 1, 15, 10, 0);
        LocalDateTime arrival = departure.plusMinutes(135);

        String request = objectMapper.writeValueAsString(
                createFlightRequest(
                        "AH-TEST101",
                        departure,
                        arrival,
                        "SCHEDULED"));

        String response = mockMvc.perform(post("/api/flights")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.flightNumber").value("AH-TEST101"))
                .andExpect(jsonPath("$.aircraftId").value(aircraft.getId()))
                .andExpect(jsonPath("$.routeId").value(route.getId()))
                .andExpect(jsonPath("$.departureAirportId")
                        .value(departureAirport.getId()))
                .andExpect(jsonPath("$.arrivalAirportId")
                        .value(arrivalAirport.getId()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long flightId = objectMapper.readTree(response)
                .get("id")
                .asLong();

        // Confirm the flight was persisted in PostgreSQL.
        org.junit.jupiter.api.Assertions.assertTrue(
                flightRepository.existsById(flightId));

        mockMvc.perform(get("/api/flights/{id}", flightId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flightId))
                .andExpect(jsonPath("$.flightNumber").value("AH-TEST101"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        // Also verify the persisted record appears in the collection.
        mockMvc.perform(get("/api/flights")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber")
                        .value("AH-TEST101"));
    }

    @Test
    void updateFlightStatus_shouldPersistValidStatusTransition()
            throws Exception {

        LocalDateTime departure = LocalDateTime.of(2027, 1, 16, 10, 0);
        LocalDateTime arrival = departure.plusMinutes(135);

        String createRequest = objectMapper.writeValueAsString(
                createFlightRequest(
                        "AH-TEST102",
                        departure,
                        arrival,
                        "SCHEDULED"));

        String response = mockMvc.perform(post("/api/flights")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long flightId = objectMapper.readTree(response)
                .get("id")
                .asLong();

        String statusRequest = """
                {
                    "status": "BOARDING"
                }
                """;

        mockMvc.perform(patch("/api/flights/{id}/status", flightId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flightId))
                .andExpect(jsonPath("$.status").value("BOARDING"));

        // Verify the status change persisted to the database.
        org.junit.jupiter.api.Assertions.assertEquals(
                "BOARDING",
                flightRepository.findById(flightId)
                        .orElseThrow()
                        .getStatus());
    }

    private Airport createAirport(
            String iataCode,
            String icaoCode,
            String name,
            String city,
            String country) {

        Airport airport = new Airport();
        airport.setIataCode(iataCode);
        airport.setIcaoCode(icaoCode);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setTerminalCount(3);
        airport.setStatus("ACTIVE");

        return airportRepository.save(airport);
    }

    private java.util.Map<String, Object> createFlightRequest(
            String flightNumber,
            LocalDateTime departure,
            LocalDateTime arrival,
            String status) {

        return java.util.Map.of(
                "flightNumber", flightNumber,
                "aircraftId", aircraft.getId(),
                "routeId", route.getId(),
                "departureAirportId", departureAirport.getId(),
                "arrivalAirportId", arrivalAirport.getId(),
                "scheduledDeparture", departure,
                "scheduledArrival", arrival,
                "status", status);
    }
}
