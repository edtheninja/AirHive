package com.airhive.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.AirportRequestDTO;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AirportRepository;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

        @Mock
        private AirportRepository airportRepository;

        private AirportService airportService;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                airportService = new AirportService(airportRepository);
        }

        private AirportRequestDTO validRequest() {

                AirportRequestDTO request = new AirportRequestDTO();

                request.setIataCode("DEL");
                request.setIcaoCode("VIDP");
                request.setName("Indira Gandhi International Airport");
                request.setCity("Delhi");
                request.setCountry("India");
                request.setTerminalCount(3);
                request.setStatus("ACTIVE");

                return request;
        }

        @Test
        void getAllAirports_shouldReturnAllAirports() {

                Airport airport1 = new Airport();
                airport1.setIataCode("DEL");

                Airport airport2 = new Airport();
                airport2.setIataCode("BOM");

                when(airportRepository.findAll())
                                .thenReturn(List.of(airport1, airport2));

                List<Airport> result = airportService.getAllAirports();

                assertEquals(2, result.size());
                assertEquals("DEL", result.get(0).getIataCode());
                assertEquals("BOM", result.get(1).getIataCode());

                verify(airportRepository).findAll();
        }

        @Test
        void getAirportById_shouldReturnAirport_whenExists() {

                Airport airport = new Airport();
                airport.setIataCode("DEL");
                airport.setIcaoCode("VIDP");
                airport.setName("Indira Gandhi International Airport");

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.of(airport));

                Airport result = airportService.getAirportById(1L);

                assertEquals("DEL", result.getIataCode());
                assertEquals("VIDP", result.getIcaoCode());
                assertEquals(
                                "Indira Gandhi International Airport",
                                result.getName());

                verify(airportRepository).findById(1L);
        }

        @Test
        void getAirportById_shouldThrowException_whenNotFound() {

                when(airportRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> airportService.getAirportById(99L));

                assertEquals(
                                "Airport not found with id: 99",
                                exception.getMessage());

                verify(airportRepository).findById(99L);
        }

        @Test
        void createAirport_shouldSaveAirport_whenRequestIsValid() {

                AirportRequestDTO request = validRequest();

                Airport savedAirport = new Airport();
                savedAirport.setIataCode("DEL");
                savedAirport.setIcaoCode("VIDP");
                savedAirport.setName(
                                "Indira Gandhi International Airport");
                savedAirport.setCity("Delhi");
                savedAirport.setCountry("India");
                savedAirport.setTerminalCount(3);
                savedAirport.setStatus("ACTIVE");

                when(airportRepository.existsByIataCode("DEL"))
                                .thenReturn(false);

                when(airportRepository.existsByIcaoCode("VIDP"))
                                .thenReturn(false);

                when(airportRepository.save(any(Airport.class)))
                                .thenReturn(savedAirport);

                Airport result = airportService.createAirport(request);

                assertEquals("DEL", result.getIataCode());
                assertEquals("VIDP", result.getIcaoCode());
                assertEquals("Delhi", result.getCity());
                assertEquals(3, result.getTerminalCount());

                verify(airportRepository)
                                .existsByIataCode("DEL");

                verify(airportRepository)
                                .existsByIcaoCode("VIDP");

                verify(airportRepository)
                                .save(any(Airport.class));
        }

        @Test
        void createAirport_shouldThrowException_whenIataAlreadyExists() {

                AirportRequestDTO request = validRequest();

                when(airportRepository.existsByIataCode("DEL"))
                                .thenReturn(true);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> airportService.createAirport(request));

                assertEquals(
                                "Airport already exists with IATA code: DEL",
                                exception.getMessage());

                verify(airportRepository)
                                .existsByIataCode("DEL");

                verify(airportRepository, never())
                                .existsByIcaoCode(anyString());

                verify(airportRepository, never())
                                .save(any(Airport.class));
        }

        @Test
        void createAirport_shouldThrowException_whenIcaoAlreadyExists() {

                AirportRequestDTO request = validRequest();

                when(airportRepository.existsByIataCode("DEL"))
                                .thenReturn(false);

                when(airportRepository.existsByIcaoCode("VIDP"))
                                .thenReturn(true);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> airportService.createAirport(request));

                assertEquals(
                                "Airport already exists with ICAO code: VIDP",
                                exception.getMessage());

                verify(airportRepository)
                                .existsByIataCode("DEL");

                verify(airportRepository)
                                .existsByIcaoCode("VIDP");

                verify(airportRepository, never())
                                .save(any(Airport.class));
        }

        @Test
        void createAirport_shouldThrowException_whenIataCodeIsMissing() {

                AirportRequestDTO request = validRequest();
                request.setIataCode("");

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> airportService.createAirport(request));

                assertEquals(
                                "IATA code is required",
                                exception.getMessage());

                verify(airportRepository, never())
                                .save(any(Airport.class));
        }

        @Test
        void updateAirport_shouldUpdate_whenRequestIsValid() {

                Airport existing = new Airport();

                existing.setIataCode("DEL");
                existing.setIcaoCode("VIDP");
                existing.setName("Old Name");
                existing.setCity("Delhi");
                existing.setCountry("India");
                existing.setTerminalCount(2);
                existing.setStatus("ACTIVE");

                AirportRequestDTO request = validRequest();

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.of(existing));

                when(airportRepository.save(existing))
                                .thenReturn(existing);

                Airport result = airportService.updateAirport(1L, request);

                assertEquals("DEL", result.getIataCode());
                assertEquals("VIDP", result.getIcaoCode());
                assertEquals(
                                "Indira Gandhi International Airport",
                                result.getName());
                assertEquals(3, result.getTerminalCount());

                verify(airportRepository).findById(1L);
                verify(airportRepository).save(existing);
        }

        @Test
        void updateAirport_shouldThrowException_whenIcaoAlreadyExists() {

                Airport existing = new Airport();

                existing.setIataCode("BOM");
                existing.setIcaoCode("VABB");

                AirportRequestDTO request = validRequest();

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.of(existing));

                when(airportRepository.existsByIataCode("DEL"))
                                .thenReturn(false);

                when(airportRepository.existsByIcaoCode("VIDP"))
                                .thenReturn(true);

                DuplicateResourceException exception = assertThrows(
                                DuplicateResourceException.class,
                                () -> airportService.updateAirport(1L, request));

                assertEquals(
                                "Airport already exists with ICAO code: VIDP",
                                exception.getMessage());

                verify(airportRepository)
                                .existsByIataCode("DEL");

                verify(airportRepository)
                                .existsByIcaoCode("VIDP");

                verify(airportRepository, never())
                                .save(any(Airport.class));
        }

        @Test
        void updateAirport_shouldThrowException_whenAirportNotFound() {

                AirportRequestDTO request = validRequest();

                when(airportRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> airportService.updateAirport(99L, request));

                assertEquals(
                                "Airport not found with id: 99",
                                exception.getMessage());

                verify(airportRepository).findById(99L);

                verify(airportRepository, never())
                                .save(any(Airport.class));
        }

        @Test
        void deleteAirport_shouldDelete_whenAirportExists() {

                Airport airport = new Airport();
                airport.setIataCode("DEL");

                when(airportRepository.findById(1L))
                                .thenReturn(Optional.of(airport));

                airportService.deleteAirport(1L);

                verify(airportRepository).findById(1L);
                verify(airportRepository).delete(airport);
        }

        @Test
        void deleteAirport_shouldThrowException_whenAirportDoesNotExist() {

                when(airportRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> airportService.deleteAirport(99L));

                assertEquals(
                                "Airport not found with id: 99",
                                exception.getMessage());

                verify(airportRepository).findById(99L);

                verify(airportRepository, never())
                                .delete(any(Airport.class));
        }
}