package com.airhive.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftTypeRepository;

@ExtendWith(MockitoExtension.class)
class AircraftTypeServiceTest {

    @Mock
    private AircraftTypeRepository aircraftTypeRepository;

    @InjectMocks
    private AircraftTypeService aircraftTypeService;

    private AircraftType aircraftType;
    private AircraftTypeRequestDTO request;

    @BeforeEach
    void setUp() {

        aircraftType = new AircraftType();

        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);

        request = new AircraftTypeRequestDTO();

        request.setTypeCode("A320");
        request.setManufacturer("Airbus");
        request.setModel("A320-200");
        request.setPassengerCapacity(180);
        request.setCrewCapacity(6);
    }

    @Test
    void getAllAircraftTypes_shouldReturnAircraftTypes() {

        when(aircraftTypeRepository.findAll())
                .thenReturn(List.of(aircraftType));

        List<AircraftTypeResponseDTO> result =
                aircraftTypeService.getAllAircraftTypes();

        assertEquals(1, result.size());
        assertEquals("A320", result.get(0).getTypeCode());
        assertEquals("Airbus", result.get(0).getManufacturer());
        assertEquals("A320-200", result.get(0).getModel());
        assertEquals(180, result.get(0).getPassengerCapacity());
        assertEquals(6, result.get(0).getCrewCapacity());
    }

    @Test
    void getAircraftTypeById_shouldReturnAircraftType() {

        when(aircraftTypeRepository.findById(1L))
                .thenReturn(Optional.of(aircraftType));

        AircraftTypeResponseDTO result =
                aircraftTypeService.getAircraftTypeById(1L);

        assertEquals("A320", result.getTypeCode());
        assertEquals("Airbus", result.getManufacturer());
        assertEquals("A320-200", result.getModel());
        assertEquals(180, result.getPassengerCapacity());
        assertEquals(6, result.getCrewCapacity());
    }

    @Test
    void getAircraftTypeById_shouldThrow_whenNotFound() {

        when(aircraftTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> aircraftTypeService.getAircraftTypeById(999L)
        );
    }

    @Test
    void createAircraftType_shouldReturnCreatedAircraftType() {

        when(aircraftTypeRepository.save(any(AircraftType.class)))
                .thenReturn(aircraftType);

        AircraftTypeResponseDTO result =
                aircraftTypeService.createAircraftType(request);

        assertEquals("A320", result.getTypeCode());
        assertEquals("Airbus", result.getManufacturer());
        assertEquals("A320-200", result.getModel());
        assertEquals(180, result.getPassengerCapacity());
        assertEquals(6, result.getCrewCapacity());

        verify(aircraftTypeRepository).save(any(AircraftType.class));
    }

    @Test
    void updateAircraftType_shouldReturnUpdatedAircraftType() {

        when(aircraftTypeRepository.findById(1L))
                .thenReturn(Optional.of(aircraftType));

        when(aircraftTypeRepository.save(any(AircraftType.class)))
                .thenReturn(aircraftType);

        AircraftTypeResponseDTO result =
                aircraftTypeService.updateAircraftType(1L, request);

        assertEquals("A320", result.getTypeCode());
        assertEquals("Airbus", result.getManufacturer());
        assertEquals("A320-200", result.getModel());

        verify(aircraftTypeRepository).save(aircraftType);
    }

    @Test
    void updateAircraftType_shouldThrow_whenNotFound() {

        when(aircraftTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> aircraftTypeService.updateAircraftType(999L, request)
        );
    }

    @Test
    void deleteAircraftType_shouldDeleteAircraftType() {

        when(aircraftTypeRepository.findById(1L))
                .thenReturn(Optional.of(aircraftType));

        aircraftTypeService.deleteAircraftType(1L);

        verify(aircraftTypeRepository).delete(aircraftType);
    }

    @Test
    void deleteAircraftType_shouldThrow_whenNotFound() {

        when(aircraftTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> aircraftTypeService.deleteAircraftType(999L)
        );
    }
}