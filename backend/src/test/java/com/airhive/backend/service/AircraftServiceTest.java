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
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftRepository;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

        @Mock
        private AircraftRepository aircraftRepository;

        @Mock
        private AircraftTypeService aircraftTypeService;

        private AircraftService aircraftService;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                aircraftService = new AircraftService(
                                aircraftRepository,
                                aircraftTypeService);
        }

        @Test
        void getAircraftById_shouldReturnAircraft_whenAircraftExists() {

                Aircraft aircraft = new Aircraft();
                aircraft.setRegistrationNumber("VT-AIR01");
                aircraft.setStatus("ACTIVE");

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(aircraft));

                Aircraft result = aircraftService.getAircraftById(1L);

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

                Aircraft aircraft2 = new Aircraft();
                aircraft2.setRegistrationNumber("VT-AIR02");

                when(aircraftRepository.findAll())
                                .thenReturn(List.of(aircraft1, aircraft2));

                List<Aircraft> result = aircraftService.getAllAircraft();

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

                when(aircraftRepository.existsByRegistrationNumber("VT-TEST01"))
                                .thenReturn(false);

                when(aircraftRepository.save(aircraft))
                                .thenReturn(aircraft);

                Aircraft result = aircraftService.createAircraft(aircraft);

                assertEquals("VT-TEST01",
                                result.getRegistrationNumber());

                verify(aircraftRepository)
                                .existsByRegistrationNumber("VT-TEST01");

                verify(aircraftRepository)
                                .save(aircraft);
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

                verify(aircraftRepository)
                                .existsByRegistrationNumber("VT-AIR02");

                verify(aircraftRepository, never())
                                .save(any(Aircraft.class));
        }

        @Test
        void createAircraft_shouldCreateUsingRequestDTO() {

                AircraftRequestDTO request = new AircraftRequestDTO();
                request.setRegistrationNumber("VT-AIR99");
                request.setStatus("ACTIVE");
                request.setAircraftTypeId(1L);

                AircraftType aircraftType = new AircraftType();
                aircraftType.setTypeCode("A320");

                when(aircraftTypeService.findEntityById(1L))
                                .thenReturn(aircraftType);

                when(aircraftRepository.existsByRegistrationNumber("VT-AIR99"))
                                .thenReturn(false);

                when(aircraftRepository.save(any(Aircraft.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                Aircraft result = aircraftService.createAircraft(request);

                assertEquals(
                                "VT-AIR99",
                                result.getRegistrationNumber());

                assertEquals(
                                "ACTIVE",
                                result.getStatus());

                assertEquals(
                                aircraftType,
                                result.getAircraftType());

                verify(aircraftTypeService)
                                .findEntityById(1L);

                verify(aircraftRepository)
                                .existsByRegistrationNumber("VT-AIR99");

                verify(aircraftRepository)
                                .save(any(Aircraft.class));
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
                verify(aircraftRepository, never())
                                .delete(any(Aircraft.class));
        }

        @Test
        void updateAircraft_shouldUpdateAircraft_whenAircraftExists() {

                Aircraft existingAircraft = new Aircraft();
                existingAircraft.setRegistrationNumber("VT-AIR01");
                existingAircraft.setStatus("ACTIVE");

                Aircraft updatedAircraft = new Aircraft();
                updatedAircraft.setRegistrationNumber("VT-AIR99");
                updatedAircraft.setStatus("INACTIVE");

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(existingAircraft));

                when(aircraftRepository.save(existingAircraft))
                                .thenReturn(existingAircraft);

                Aircraft result = aircraftService.updateAircraft(1L, updatedAircraft);

                assertEquals(
                                "VT-AIR99",
                                result.getRegistrationNumber());

                assertEquals(
                                "INACTIVE",
                                result.getStatus());

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
                                () -> aircraftService.updateAircraft(
                                                99L,
                                                updatedAircraft));

                assertEquals(
                                "Aircraft not found with id: 99",
                                exception.getMessage());

                verify(aircraftRepository).findById(99L);

                verify(aircraftRepository, never())
                                .save(any(Aircraft.class));
        }

        @Test
        void updateAircraft_shouldUpdateUsingRequestDTO() {

                Aircraft existingAircraft = new Aircraft();
                existingAircraft.setRegistrationNumber("VT-AIR01");
                existingAircraft.setStatus("ACTIVE");

                AircraftType aircraftType = new AircraftType();

                AircraftRequestDTO request = new AircraftRequestDTO();
                request.setRegistrationNumber("VT-AIR99");
                request.setStatus("ACTIVE");
                request.setAircraftTypeId(1L);

                when(aircraftRepository.findById(1L))
                                .thenReturn(Optional.of(existingAircraft));

                when(aircraftTypeService.findEntityById(1L))
                                .thenReturn(aircraftType);

                when(aircraftRepository.save(existingAircraft))
                                .thenReturn(existingAircraft);

                Aircraft result = aircraftService.updateAircraft(1L, request);

                assertEquals(
                                "VT-AIR99",
                                result.getRegistrationNumber());

                assertEquals(
                                aircraftType,
                                result.getAircraftType());

                verify(aircraftRepository).findById(1L);
                verify(aircraftTypeService).findEntityById(1L);
                verify(aircraftRepository).save(existingAircraft);
        }

}