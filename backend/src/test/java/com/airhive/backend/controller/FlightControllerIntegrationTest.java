
package com.airhive.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
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
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FlightControllerIntegrationTest {

        @Container
        static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6");

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

        @Test
        void createFlight_shouldRejectOverlappingScheduleForSameAircraft()
                        throws Exception {

                LocalDateTime departure = LocalDateTime.of(2027, 1, 20, 10, 0);
                LocalDateTime arrival = departure.plusMinutes(135);

                String firstRequest = objectMapper.writeValueAsString(
                                createFlightRequest(
                                                "AH-OVERLAP101",
                                                departure,
                                                arrival,
                                                "SCHEDULED"));

                mockMvc.perform(post("/api/flights")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest))
                                .andExpect(status().isCreated());

                // The second flight overlaps the first flight by 30 minutes.
                LocalDateTime secondDeparture = departure.plusMinutes(105);
                LocalDateTime secondArrival = secondDeparture.plusMinutes(135);

                String secondRequest = objectMapper.writeValueAsString(
                                createFlightRequest(
                                                "AH-OVERLAP102",
                                                secondDeparture,
                                                secondArrival,
                                                "SCHEDULED"));

                mockMvc.perform(post("/api/flights")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest))
                                .andExpect(status().isConflict());

                org.junit.jupiter.api.Assertions.assertEquals(
                                1,
                                flightRepository.count());

                org.junit.jupiter.api.Assertions.assertFalse(
                                flightRepository.existsByFlightNumber("AH-OVERLAP102"));
        }

        @Test
        void createFlight_shouldAllowNonOverlappingSchedulesForSameAircraft()
                        throws Exception {

                LocalDateTime departure = LocalDateTime.of(2027, 1, 21, 10, 0);
                LocalDateTime arrival = departure.plusMinutes(135);

                String firstRequest = objectMapper.writeValueAsString(
                                createFlightRequest(
                                                "AH-SEQUENTIAL101",
                                                departure,
                                                arrival,
                                                "SCHEDULED"));

                mockMvc.perform(post("/api/flights")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest))
                                .andExpect(status().isCreated());

                // The second flight departs 30 minutes after the first arrives.
                LocalDateTime secondDeparture = arrival.plusMinutes(30);
                LocalDateTime secondArrival = secondDeparture.plusMinutes(135);

                String secondRequest = objectMapper.writeValueAsString(
                                createFlightRequest(
                                                "AH-SEQUENTIAL102",
                                                secondDeparture,
                                                secondArrival,
                                                "SCHEDULED"));

                mockMvc.perform(post("/api/flights")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.flightNumber")
                                                .value("AH-SEQUENTIAL102"));

                org.junit.jupiter.api.Assertions.assertEquals(
                                2,
                                flightRepository.count());
        }

        @Test
        void updateFlightStatus_shouldRejectInvalidTransitionAndPreserveCurrentStatus() throws Exception {
                LocalDateTime departure = LocalDateTime.of(2027, 1, 22, 10, 0);
                LocalDateTime arrival = departure.plusMinutes(135);

                String createResponse = mockMvc.perform(post("/api/flights")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                createFlightRequest(
                                                                "AH-INVALID-TRANS",
                                                                departure,
                                                                arrival,
                                                                "SCHEDULED"))))

                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                long flightId = objectMapper.readTree(createResponse)
                                .get("id")
                                .asLong();

                // SCHEDULED -> LANDED is an invalid direct transition.
                mockMvc.perform(patch("/api/flights/{id}/status", flightId)
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Map.of("status", "LANDED"))))
                                .andExpect(status().isBadRequest());

                // The rejected transition must not modify the persisted flight.
                assertEquals(
                                "SCHEDULED",
                                flightRepository.findById(flightId)
                                                .orElseThrow()
                                                .getStatus());
        }


@Test
void updateFlightStatus_shouldRejectDelayedWithoutReason() throws Exception {
    LocalDateTime departure = LocalDateTime.of(2027, 1, 23, 10, 0);
    LocalDateTime arrival = departure.plusMinutes(135);

    String createResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-DELAY-REASON",
                                    departure,
                                    arrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long flightId = objectMapper.readTree(createResponse)
            .get("id")
            .asLong();

    mockMvc.perform(patch("/api/flights/{id}/status", flightId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Map.of("status", "DELAYED")
                    )))
            .andExpect(status().isBadRequest());

    assertEquals(
            "SCHEDULED",
            flightRepository.findById(flightId)
                    .orElseThrow()
                    .getStatus()
    );
}

@Test
void updateFlightStatus_shouldRejectCancelledWithoutReason() throws Exception {
    LocalDateTime departure = LocalDateTime.of(2027, 1, 24, 10, 0);
    LocalDateTime arrival = departure.plusMinutes(135);

    String createResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-CANCEL-REASON",
                                    departure,
                                    arrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long flightId = objectMapper.readTree(createResponse)
            .get("id")
            .asLong();

    mockMvc.perform(patch("/api/flights/{id}/status", flightId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Map.of("status", "CANCELLED")
                    )))
            .andExpect(status().isBadRequest());

    assertEquals(
            "SCHEDULED",
            flightRepository.findById(flightId)
                    .orElseThrow()
                    .getStatus()
    );
}


@Test
void updateFlight_shouldPersistValidChanges() throws Exception {
    LocalDateTime departure = LocalDateTime.of(2027, 1, 25, 10, 0);
    LocalDateTime arrival = departure.plusMinutes(135);

    String createResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-UPDATE-VALID",
                                    departure,
                                    arrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long flightId = objectMapper.readTree(createResponse)
            .get("id")
            .asLong();

    LocalDateTime updatedDeparture = departure.plusMinutes(15);
    LocalDateTime updatedArrival = arrival.plusMinutes(15);

    mockMvc.perform(put("/api/flights/{id}", flightId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-UPDATE-VALID",
                                    updatedDeparture,
                                    updatedArrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isOk());

    var updatedFlight = flightRepository.findById(flightId).orElseThrow();

    assertEquals(updatedDeparture, updatedFlight.getScheduledDeparture());
    assertEquals(updatedArrival, updatedFlight.getScheduledArrival());
}

@Test
void updateFlight_shouldRejectOverlappingAircraftSchedule() throws Exception {
    LocalDateTime firstDeparture = LocalDateTime.of(2027, 1, 26, 10, 0);
    LocalDateTime firstArrival = firstDeparture.plusMinutes(135);

    String firstResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-UPDATE-OVERLAP1",
                                    firstDeparture,
                                    firstArrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long firstFlightId = objectMapper.readTree(firstResponse)
            .get("id")
            .asLong();

    LocalDateTime secondDeparture = LocalDateTime.of(2027, 1, 26, 13, 0);
    LocalDateTime secondArrival = secondDeparture.plusMinutes(135);

    mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-UPDATE-OVERLAP2",
                                    secondDeparture,
                                    secondArrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated());

    // Move the first flight into the second flight's scheduled window.
    LocalDateTime conflictingDeparture = LocalDateTime.of(2027, 1, 26, 12, 0);
    LocalDateTime conflictingArrival = conflictingDeparture.plusMinutes(135);

    mockMvc.perform(put("/api/flights/{id}", firstFlightId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-UPDATE-OVERLAP1",
                                    conflictingDeparture,
                                    conflictingArrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isConflict());

    var unchangedFlight = flightRepository.findById(firstFlightId).orElseThrow();

    assertEquals(firstDeparture, unchangedFlight.getScheduledDeparture());
    assertEquals(firstArrival, unchangedFlight.getScheduledArrival());
}

@Test
void deleteFlight_shouldRejectFlightWithActivityRecords() throws Exception {
    LocalDateTime departure = LocalDateTime.of(2027, 1, 27, 10, 0);
    LocalDateTime arrival = departure.plusMinutes(135);

    String createResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            createFlightRequest(
                                    "AH-DELETE-ACTIVITY",
                                    departure,
                                    arrival,
                                    "SCHEDULED"
                            )
                    )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long flightId = objectMapper.readTree(createResponse)
            .get("id")
            .asLong();

    assertTrue(flightActivityRepository.existsByFlightId(flightId));

    mockMvc.perform(delete("/api/flights/{id}", flightId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isConflict());

    assertTrue(flightRepository.findById(flightId).isPresent());
}

@Test
void getFlightActivities_shouldReturnCreationAndStatusChangeEvents()
        throws Exception {

    LocalDateTime departure = LocalDateTime.of(2027, 1, 17, 10, 0);
    LocalDateTime arrival = departure.plusMinutes(135);

    String createRequest = objectMapper.writeValueAsString(
            createFlightRequest(
                    "AH-ACTIVITY01",
                    departure,
                    arrival,
                    "SCHEDULED"));

    String createResponse = mockMvc.perform(post("/api/flights")
                    .with(jwt().authorities(
                            new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long flightId = objectMapper.readTree(createResponse)
            .get("id")
            .asLong();

    mockMvc.perform(patch("/api/flights/{id}/status", flightId)
                    .with(jwt().authorities(
                            new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "status": "DELAYED",
                                "reason": "Weather conditions"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELAYED"));

    mockMvc.perform(get("/api/flights/{id}/activities", flightId)
                    .with(jwt().authorities(
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].eventType").value("STATUS_CHANGED"))
            .andExpect(jsonPath("$[0].previousStatus").value("SCHEDULED"))
            .andExpect(jsonPath("$[0].currentStatus").value("DELAYED"))
            .andExpect(jsonPath("$[0].reason").value("Weather conditions"))
            .andExpect(jsonPath("$[1].eventType").value("FLIGHT_CREATED"))
            .andExpect(jsonPath("$[1].currentStatus").value("SCHEDULED"));
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
