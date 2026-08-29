package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.AircraftType;

@DataJpaTest
class AircraftTypeRepositoryTest {

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    @Test
    void save_shouldPersistAircraftType() {

        AircraftType aircraftType = new AircraftType();

        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);

        AircraftType saved =
                aircraftTypeRepository.saveAndFlush(aircraftType);

        assertNotNull(saved.getId());
        assertEquals("A320", saved.getTypeCode());
        assertEquals("Airbus", saved.getManufacturer());
        assertEquals("A320-200", saved.getModel());
    }

    @Test
    void findById_shouldReturnAircraftType_whenExists() {

        AircraftType aircraftType = new AircraftType();

        aircraftType.setTypeCode("B737");
        aircraftType.setManufacturer("Boeing");
        aircraftType.setModel("737-800");
        aircraftType.setPassengerCapacity(189);
        aircraftType.setCrewCapacity(6);

        AircraftType saved =
                aircraftTypeRepository.saveAndFlush(aircraftType);

        var result =
                aircraftTypeRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(
                saved.getId(),
                result.get().getId());
        assertEquals(
                "B737",
                result.get().getTypeCode());
    }

    @Test
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {

        var result =
                aircraftTypeRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }
}