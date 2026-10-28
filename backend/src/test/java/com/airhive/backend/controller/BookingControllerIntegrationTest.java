package com.airhive.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

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

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Booking;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.BookingRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookingControllerIntegrationTest {

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
    private BookingRepository bookingRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAllInBatch();
    }

    @Test
    void getAllBookingsShouldReturnEmptyArrayWhenNoBookingsExist()
            throws Exception {

        mockMvc.perform(get("/api/bookings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllBookingsShouldReturnPersistedBookingWithCorrectFields()
            throws Exception {

        Booking booking = new Booking();
        booking.setPnr("AHITEST001");
        booking.setPassenger("Test Passenger");
        booking.setFlight("AH101");
        booking.setRoute("DEL-BOM");
        booking.setCabin("ECONOMY");
        booking.setSeat("12A");
        booking.setStatus("CONFIRMED");
        booking.setAmount(new BigDecimal("1499.50"));
        booking.setTravelDate(LocalDate.of(2026, 11, 15));

        Booking savedBooking = bookingRepository.saveAndFlush(booking);

        mockMvc.perform(get("/api/bookings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(
                        savedBooking.getId().intValue()))
                .andExpect(jsonPath("$[0].pnr").value("AHITEST001"))
                .andExpect(jsonPath("$[0].passenger").value("Test Passenger"))
                .andExpect(jsonPath("$[0].flight").value("AH101"))
                .andExpect(jsonPath("$[0].route").value("DEL-BOM"))
                .andExpect(jsonPath("$[0].cabin").value("ECONOMY"))
                .andExpect(jsonPath("$[0].seat").value("12A"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].amount").value(1499.50))
                .andExpect(jsonPath("$[0].travelDate").value("2026-11-15"));
    }

    @Test
    void passengerCanOnlyReadBookingsAssignedToTheirAccount() throws Exception {
        AppUser owner = createPassenger("booking-owner");
        AppUser other = createPassenger("booking-other");

        Booking ownedBooking = createBooking("OWNEDPNR001", owner);
        createBooking("OTHERPNR001", other);
        createBooking("LEGACYPNR001", null);

        mockMvc.perform(get("/api/bookings")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject(owner.getUsername())
                                        .claim("userId", owner.getId())
                                        .claim("role", "PASSENGER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PASSENGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ownedBooking.getId().intValue()))
                .andExpect(jsonPath("$[0].pnr").value("OWNEDPNR001"));
    }

    private AppUser createPassenger(String prefix) {
        AppUser user = new AppUser();
        user.setUsername(prefix + "-" + System.nanoTime());
        user.setPassword("test-password-hash");
        user.setRole(Role.PASSENGER);
        user.setEnabled(true);
        return appUserRepository.saveAndFlush(user);
    }

    private Booking createBooking(String pnr, AppUser owner) {
        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setPassenger("Test Passenger");
        booking.setFlight("AH101");
        booking.setRoute("DEL-BOM");
        booking.setCabin("ECONOMY");
        booking.setSeat("12A");
        booking.setStatus("CONFIRMED");
        booking.setAmount(new BigDecimal("1499.50"));
        booking.setTravelDate(LocalDate.of(2026, 11, 15));
        booking.setUser(owner);
        return bookingRepository.saveAndFlush(booking);
    }
}
