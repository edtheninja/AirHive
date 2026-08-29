package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;

@DataJpaTest
class AircraftRepositoryTest {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    private AircraftType aircraftType;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        aircraftType = new AircraftType();
        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);

        aircraftType =
                aircraftTypeRepository.saveAndFlush(aircraftType);
    }

    @Test
    void findByRegistrationNumber_shouldReturnAircraft_whenRegistrationExists() {

        Aircraft aircraft = saveAircraft("VT-TEST01");

        var result =
                aircraftRepository.findByRegistrationNumber("VT-TEST01");

        assertTrue(result.isPresent());
        assertEquals(
                aircraft.getId(),
                result.get().getId());
        assertEquals(
                "VT-TEST01",
                result.get().getRegistrationNumber());
    }

    @Test
    void findByRegistrationNumber_shouldReturnEmpty_whenRegistrationDoesNotExist() {

        var result =
                aircraftRepository.findByRegistrationNumber("VT-NOTFOUND");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByRegistrationNumber_shouldReturnTrue_whenRegistrationExists() {

        saveAircraft("VT-TEST02");

        assertTrue(
                aircraftRepository.existsByRegistrationNumber(
                        "VT-TEST02"));
    }

    @Test
    void existsByRegistrationNumber_shouldReturnFalse_whenRegistrationDoesNotExist() {

        assertFalse(
                aircraftRepository.existsByRegistrationNumber(
                        "VT-NOTFOUND"));
    }

    private Aircraft saveAircraft(String registrationNumber) {

        Aircraft aircraft = new Aircraft();

        aircraft.setRegistrationNumber(registrationNumber);
        aircraft.setAircraftType(aircraftType);
        aircraft.setStatus("ACTIVE");

        return aircraftRepository.saveAndFlush(aircraft);
    }
}