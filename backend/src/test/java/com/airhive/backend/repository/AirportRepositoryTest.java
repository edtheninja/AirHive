package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.Airport;

@DataJpaTest
class AirportRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        Airport airport = new Airport();

        airport.setIataCode("TST");
        airport.setIcaoCode("VTST");
        airport.setName("Test Airport");
        airport.setCity("Test City");
        airport.setCountry("India");
        airport.setTerminalCount(1);
        airport.setStatus("ACTIVE");

        airportRepository.saveAndFlush(airport);
    }

    @Test
    void existsByIataCode_shouldReturnTrue_whenCodeExists() {

        assertTrue(
                airportRepository.existsByIataCode("TST"));
    }

    @Test
    void existsByIataCode_shouldReturnFalse_whenCodeDoesNotExist() {

        assertFalse(
                airportRepository.existsByIataCode("XXX"));
    }

    @Test
    void existsByIcaoCode_shouldReturnTrue_whenCodeExists() {

        assertTrue(
                airportRepository.existsByIcaoCode("VTST"));
    }

    @Test
    void existsByIcaoCode_shouldReturnFalse_whenCodeDoesNotExist() {

        assertFalse(
                airportRepository.existsByIcaoCode("VXXX"));
    }
}