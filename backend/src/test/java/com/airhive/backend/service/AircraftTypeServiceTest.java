package com.airhive.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftTypeRepository;

@ExtendWith(MockitoExtension.class)
class AircraftTypeServiceTest {

        @Mock
        private AircraftTypeRepository aircraftTypeRepository;

        private AircraftTypeService aircraftTypeService;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                aircraftTypeService = new AircraftTypeService(aircraftTypeRepository);
        }

        @Test
        void getAllAircraftTypes_shouldReturnAllAircraftTypes() {

                AircraftType type1 = new AircraftType();
                type1.setTypeCode("B738");

                AircraftType type2 = new AircraftType();
                type2.setTypeCode("A320");

                when(aircraftTypeRepository.findAll())
                                .thenReturn(List.of(type1, type2));

                List<AircraftType> result = aircraftTypeService.getAllAircraftTypes();

                assertEquals(2, result.size());
                assertEquals("B738", result.get(0).getTypeCode());
                assertEquals("A320", result.get(1).getTypeCode());

                verify(aircraftTypeRepository).findAll();
        }

        @Test
        void getAircraftTypeById_shouldReturnAircraftType_whenExists() {

                AircraftType aircraftType = new AircraftType();
                aircraftType.setTypeCode("B738");
                aircraftType.setManufacturer("Boeing");
                aircraftType.setModel("737-800");

                when(aircraftTypeRepository.findById(1L))
                                .thenReturn(Optional.of(aircraftType));

                AircraftType result = aircraftTypeService.getAircraftTypeById(1L);

                assertEquals("B738", result.getTypeCode());
                assertEquals("Boeing", result.getManufacturer());
                assertEquals("737-800", result.getModel());

                verify(aircraftTypeRepository).findById(1L);
        }

        @Test
        void getAircraftTypeById_shouldThrowException_whenNotFound() {

                when(aircraftTypeRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftTypeService
                                                .getAircraftTypeById(99L));

                assertEquals(
                                "Aircraft type not found with id: 99",
                                exception.getMessage());

                verify(aircraftTypeRepository).findById(99L);
        }

        @Test
        void createAircraftType_shouldSaveAircraftType() {

                AircraftTypeRequestDTO request = new AircraftTypeRequestDTO();

                request.setTypeCode("B738");
                request.setManufacturer("Boeing");
                request.setModel("737-800");
                request.setPassengerCapacity(189);
                request.setCrewCapacity(2);

                AircraftType savedAircraftType = new AircraftType();

                savedAircraftType.setTypeCode("B738");
                savedAircraftType.setManufacturer("Boeing");
                savedAircraftType.setModel("737-800");
                savedAircraftType.setPassengerCapacity(189);
                savedAircraftType.setCrewCapacity(2);

                when(aircraftTypeRepository.save(any(AircraftType.class)))
                                .thenReturn(savedAircraftType);

                AircraftType result = aircraftTypeService.createAircraftType(request);

                assertEquals("B738", result.getTypeCode());
                assertEquals("Boeing", result.getManufacturer());
                assertEquals("737-800", result.getModel());
                assertEquals(189, result.getPassengerCapacity());
                assertEquals(2, result.getCrewCapacity());

                verify(aircraftTypeRepository)
                                .save(any(AircraftType.class));
        }

        @Test
        void updateAircraftType_shouldUpdateExistingAircraftType() {

                AircraftType existing = new AircraftType();
                existing.setTypeCode("B738");
                existing.setManufacturer("Boeing");
                existing.setModel("737-800");

                AircraftTypeRequestDTO request = new AircraftTypeRequestDTO();

                request.setTypeCode("B738");
                request.setManufacturer("Boeing");
                request.setModel("737-900");
                request.setPassengerCapacity(215);
                request.setCrewCapacity(2);

                when(aircraftTypeRepository.findById(1L))
                                .thenReturn(Optional.of(existing));

                when(aircraftTypeRepository.save(existing))
                                .thenReturn(existing);

                AircraftType result = aircraftTypeService.updateAircraftType(1L, request);

                assertEquals("B738", result.getTypeCode());
                assertEquals("Boeing", result.getManufacturer());
                assertEquals("737-900", result.getModel());
                assertEquals(215, result.getPassengerCapacity());
                assertEquals(2, result.getCrewCapacity());

                verify(aircraftTypeRepository).findById(1L);
                verify(aircraftTypeRepository).save(existing);
        }

        @Test
        void updateAircraftType_shouldThrowException_whenNotFound() {

                AircraftTypeRequestDTO request = new AircraftTypeRequestDTO();

                request.setTypeCode("B738");
                request.setManufacturer("Boeing");
                request.setModel("737-800");
                request.setPassengerCapacity(189);
                request.setCrewCapacity(2);

                when(aircraftTypeRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftTypeService
                                                .updateAircraftType(99L, request));

                assertEquals(
                                "Aircraft type not found with id: 99",
                                exception.getMessage());

                verify(aircraftTypeRepository).findById(99L);

                verify(aircraftTypeRepository, never())
                                .save(any(AircraftType.class));
        }

        @Test
        void deleteAircraftType_shouldDelete_whenExists() {

                AircraftType existing = new AircraftType();
                existing.setTypeCode("B738");

                when(aircraftTypeRepository.findById(1L))
                                .thenReturn(Optional.of(existing));

                aircraftTypeService.deleteAircraftType(1L);

                verify(aircraftTypeRepository).findById(1L);
                verify(aircraftTypeRepository).delete(existing);
        }

        @Test
        void deleteAircraftType_shouldThrowException_whenNotFound() {

                when(aircraftTypeRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftTypeService.deleteAircraftType(99L));

                assertEquals(
                                "Aircraft type not found with id: 99",
                                exception.getMessage());

                verify(aircraftTypeRepository).findById(99L);

                verify(aircraftTypeRepository, never())
                                .delete(any(AircraftType.class));
        }
        
}