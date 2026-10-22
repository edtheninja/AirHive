
package com.airhive.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.airhive.backend.entity.Passenger;
import com.airhive.backend.repository.PassengerRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PassengerControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6");

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
    private PassengerRepository passengerRepository;

    @BeforeEach
    void setUp() {
        passengerRepository.deleteAllInBatch();
    }

    @Test
    void getAllPassengersShouldReturnEmptyArrayWhenNoPassengersExist()
            throws Exception {

        mockMvc.perform(get("/api/passengers")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllPassengersShouldReturnPersistedPassengerWithCorrectFields()
            throws Exception {

        Passenger passenger = new Passenger();
        passenger.setPassengerCode("PAX-TEST-001");
        passenger.setName("Test Passenger");
        passenger.setTier("GOLD");
        passenger.setFlight("AH101");
        passenger.setRoute("DEL-BOM");
        passenger.setSeat("12A");
        passenger.setBags(2);
        passenger.setCheckedIn(true);

        Passenger savedPassenger = passengerRepository.saveAndFlush(passenger);

        mockMvc.perform(get("/api/passengers")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(
                        savedPassenger.getId().intValue()))
                .andExpect(jsonPath("$[0].passengerCode").value("PAX-TEST-001"))
                .andExpect(jsonPath("$[0].name").value("Test Passenger"))
                .andExpect(jsonPath("$[0].tier").value("GOLD"))
                .andExpect(jsonPath("$[0].flight").value("AH101"))
                .andExpect(jsonPath("$[0].route").value("DEL-BOM"))
                .andExpect(jsonPath("$[0].seat").value("12A"))
                .andExpect(jsonPath("$[0].bags").value(2))
                .andExpect(jsonPath("$[0].checkedIn").value(true));
    }
}
