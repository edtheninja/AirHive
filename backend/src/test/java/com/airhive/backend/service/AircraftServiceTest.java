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

import com.airhive.backend.dto.AircraftRequestDTO;
import com.airhive.backend.dto.AircraftResponseDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceInUseException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.FlightRepository;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

        @Mock
        private AircraftRepository aircraftRepository;

        @Mock
        private AircraftTypeService aircraftTypeService;

        @Mock
        private FlightRepository flightRepository;

        private AircraftService aircraftService;

        private AircraftType defaultType;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                aircraftService = new AircraftService(
                aircraftRepository,
                flightRepository,
                aircraftTypeService);
                defaultType = new AircraftType();
                defaultType.setTypeCode("A320");
        }

        @Test
        void getAircraftById_shouldReturnAircraft_whenAircraftExists() {

                Aircraft aircraft = new Aircraft();
                aircraft.setRegistrationNumber("VT-AIR01");
                aircraft.setStatus("ACTIVE");
                aircraft.setAircraftType(defaultType);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(aircraft));

                AircraftResponseDTO result = aircraftService.getAircraftById(1L);

                assertEquals("VT-AIR01", result.getRegistrationNumber());
                assertEquals("ACTIVE", result.getStatus());

                verify(aircraftRepository).findById(1L);
        }

        @Test
        void getAircraftById_shouldThrowException_whenAircraftDoesNotExist() {

                when(aircraftRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftService.getAircraftById(99L));

                assertEquals(
                                "Aircraft not found with id: 99",
                                exception.getMessage());

                verify(aircraftRepository).findById(99L);
        }

        @Test
        void getAllAircraft_shouldReturnAllAircraft() {

                Aircraft aircraft1 = new Aircraft();
                aircraft1.setRegistrationNumber("VT-AIR01");
                aircraft1.setAircraftType(defaultType);

                Aircraft aircraft2 = new Aircraft();
                aircraft2.setRegistrationNumber("VT-AIR02");
                aircraft2.setAircraftType(defaultType);

                when(aircraftRepository.findAll())
                                .thenReturn(List.of(aircraft1, aircraft2));

                List<AircraftResponseDTO> result = aircraftService.getAllAircraft();

                assertEquals(2, result.size());
                assertEquals("VT-AIR01",
                                result.get(0).getRegistrationNumber());
                assertEquals("VT-AIR02",
                                result.get(1).getRegistrationNumber());

                verify(aircraftRepository).findAll();
        }

        @Test
        void createAircraft_shouldSaveAircraft_whenRegistrationIsUnique() {

                Aircraft aircraft = new Aircraft();
                aircraft.setRegistrationNumber("VT-TEST01");
                aircraft.setStatus("ACTIVE");
                aircraft.setAircraftType(defaultType);

                when(aircraftRepository.existsByRegistrationNumber("VT-TEST01"))
                                .thenReturn(false);

                when(aircraftRepository.save(aircraft))
                                .thenReturn(aircraft);

                AircraftResponseDTO result = aircraftService.createAircraft(aircraft);

                assertEquals("VT-TEST01", result.getRegistrationNumber());

                verify(aircraftRepository).existsByRegistrationNumber("VT-TEST01");
                verify(aircraftRepository).save(aircraft);
        }

        @Test
        void createAircraft_shouldThrowException_whenRegistrationAlreadyExists() {

                Aircraft aircraft = new Aircraft();
                aircraft.setRegistrationNumber("VT-AIR02");

                when(aircraftRepository.existsByRegistrationNumber("VT-AIR02"))
                                .thenReturn(true);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> aircraftService.createAircraft(aircraft));

                assertEquals(
                                "Aircraft already exists with registration number: VT-AIR02",
                                exception.getMessage());

                verify(aircraftRepository).existsByRegistrationNumber("VT-AIR02");
                verify(aircraftRepository, never()).save(any(Aircraft.class));
        }

        @Test
        void createAircraft_shouldCreateUsingRequestDTO() {

                AircraftRequestDTO request = new AircraftRequestDTO();
                request.setRegistrationNumber("VT-AIR99");
                request.setStatus("ACTIVE");
                request.setAircraftTypeId(1L);

                when(aircraftTypeService.findEntityById(1L))
                                .thenReturn(defaultType);

                when(aircraftRepository.existsByRegistrationNumber("VT-AIR99"))
                                .thenReturn(false);

                when(aircraftRepository.save(any(Aircraft.class)))
                                .thenAnswer(invocation -> {
                                        Aircraft a = invocation.getArgument(0);
                                        a.setAircraftType(defaultType);
                                        return a;
                                });

                AircraftResponseDTO result = aircraftService.createAircraft(request);

                assertEquals("VT-AIR99", result.getRegistrationNumber());
                assertEquals("ACTIVE", result.getStatus());
                assertEquals("A320", result.getAircraftTypeCode());

                verify(aircraftTypeService).findEntityById(1L);
                verify(aircraftRepository).existsByRegistrationNumber("VT-AIR99");
                verify(aircraftRepository).save(any(Aircraft.class));
        }

        @Test
        void deleteAircraft_shouldDeleteAircraft_whenAircraftExists() {

                Aircraft aircraft = new Aircraft();
                aircraft.setRegistrationNumber("VT-AIR01");

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(aircraft));

                aircraftService.deleteAircraft(1L);

                verify(aircraftRepository).findById(1L);
                verify(aircraftRepository).delete(aircraft);
        }

        @Test
        void deleteAircraft_shouldThrowException_whenAircraftDoesNotExist() {

                when(aircraftRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftService.deleteAircraft(99L));

                assertEquals(
                                "Aircraft not found with id: 99",
                                exception.getMessage());

                verify(aircraftRepository).findById(99L);
                verify(aircraftRepository, never()).delete(any(Aircraft.class));
        }

        @Test
        void updateAircraft_shouldUpdateAircraft_whenAircraftExists() {

                Aircraft existingAircraft = new Aircraft();
                existingAircraft.setRegistrationNumber("VT-AIR01");
                existingAircraft.setStatus("ACTIVE");
                existingAircraft.setAircraftType(defaultType);

                Aircraft updatedAircraft = new Aircraft();
                updatedAircraft.setRegistrationNumber("VT-AIR99");
                updatedAircraft.setStatus("INACTIVE");
                updatedAircraft.setAircraftType(defaultType);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(existingAircraft));

                when(aircraftRepository.save(existingAircraft))
                                .thenReturn(existingAircraft);

                AircraftResponseDTO result = aircraftService.updateAircraft(1L, updatedAircraft);

                assertEquals("VT-AIR99", result.getRegistrationNumber());
                assertEquals("INACTIVE", result.getStatus());

                verify(aircraftRepository).findById(1L);
                verify(aircraftRepository).save(existingAircraft);
        }

        @Test
        void updateAircraft_shouldThrowException_whenAircraftDoesNotExist() {

                Aircraft updatedAircraft = new Aircraft();
                updatedAircraft.setRegistrationNumber("VT-AIR99");
                updatedAircraft.setStatus("INACTIVE");

                when(aircraftRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> aircraftService.updateAircraft(99L, updatedAircraft));

                assertEquals(
                                "Aircraft not found with id: 99",
                                exception.getMessage());

                verify(aircraftRepository).findById(99L);
                verify(aircraftRepository, never()).save(any(Aircraft.class));
        }

        @Test
        void updateAircraft_shouldUpdateUsingRequestDTO() {

                Aircraft existingAircraft = new Aircraft();
                existingAircraft.setRegistrationNumber("VT-AIR01");
                existingAircraft.setStatus("ACTIVE");
                existingAircraft.setAircraftType(defaultType);

                AircraftRequestDTO request = new AircraftRequestDTO();
                request.setRegistrationNumber("VT-AIR99");
                request.setStatus("ACTIVE");
                request.setAircraftTypeId(1L);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(existingAircraft));

                when(aircraftTypeService.findEntityById(1L))
                                .thenReturn(defaultType);

                when(aircraftRepository.save(existingAircraft))
                                .thenReturn(existingAircraft);

                AircraftResponseDTO result = aircraftService.updateAircraft(1L, request);

                assertEquals("VT-AIR99", result.getRegistrationNumber());
                assertEquals("A320", result.getAircraftTypeCode());

                verify(aircraftRepository).findById(1L);
                verify(aircraftTypeService).findEntityById(1L);
                verify(aircraftRepository).save(existingAircraft);
        }

        @Test
void deleteAircraft_shouldReject_whenAircraftIsReferencedByFlight() {

        Aircraft aircraft = new Aircraft();

        when(aircraftRepository.findById(1L))
                        .thenReturn(Optional.of(aircraft));

        when(flightRepository.existsByAircraftId(1L))
                        .thenReturn(true);

        ResourceInUseException exception = assertThrows(
                        ResourceInUseException.class,
                        () -> aircraftService.deleteAircraft(1L));

        assertEquals(
                        "Aircraft cannot be deleted because it is referenced by one or more flights",
                        exception.getMessage());

        verify(flightRepository).existsByAircraftId(1L);
        verify(aircraftRepository, never()).delete(any(Aircraft.class));
}

@Test
void deleteAircraft_shouldDelete_whenAircraftIsNotReferencedByFlight() {

        Aircraft aircraft = new Aircraft();

        when(aircraftRepository.findById(1L))
                        .thenReturn(Optional.of(aircraft));

        when(flightRepository.existsByAircraftId(1L))
                        .thenReturn(false);

        aircraftService.deleteAircraft(1L);

        verify(flightRepository).existsByAircraftId(1L);
        verify(aircraftRepository).delete(aircraft);
}

}
