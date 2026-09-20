package com.airhive.backend.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AircraftRequestDTO;
import com.airhive.backend.dto.AircraftResponseDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceInUseException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.AircraftMapper;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.FlightRepository;

@Service
public class AircraftService {

        private final AircraftRepository aircraftRepository;
        private final AircraftTypeService aircraftTypeService;
        private final FlightRepository flightRepository;

        public AircraftService(
                        AircraftRepository aircraftRepository,
                        FlightRepository flightRepository,
                        AircraftTypeService aircraftTypeService) {
                this.aircraftRepository = aircraftRepository;
                this.flightRepository = flightRepository;
                this.aircraftTypeService = aircraftTypeService;
        }

        @Cacheable("aircraft")
        public List<AircraftResponseDTO> getAllAircraft() {
                return aircraftRepository.findAll()
                                .stream()
                                .map(AircraftMapper::toResponse)
                                .toList();
        }

        // Temporarily disabled until typed Redis DTO serialization is configured.
        public AircraftResponseDTO getAircraftById(Long id) {
                return AircraftMapper.toResponse(
                                aircraftRepository.findById(id)
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Aircraft not found with id: " + id)));
        }

        @CacheEvict(value = { "aircraft", "flights" }, allEntries = true)
        public AircraftResponseDTO createAircraft(Aircraft aircraft) {
                if (aircraftRepository.existsByRegistrationNumber(
                                aircraft.getRegistrationNumber())) {

                        throw new DuplicateResourceException(
                                        "Aircraft already exists with registration number: "
                                                        + aircraft.getRegistrationNumber());
                }

                return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
        }

        @CacheEvict(value = { "aircraft", "flights" }, allEntries = true)
        public AircraftResponseDTO createAircraft(AircraftRequestDTO request) {
                Aircraft aircraft = new Aircraft();
                applyRequest(aircraft, request);
                return createAircraft(aircraft);
        }

        @CacheEvict(value = { "aircraft", "flights" }, allEntries = true)
        public AircraftResponseDTO updateAircraft(Long id, Aircraft updatedAircraft) {

                Aircraft aircraft = getAircraftEntityById(id);
                if (aircraftRepository.existsByRegistrationNumberAndIdNot(
                                updatedAircraft.getRegistrationNumber(),
                                id)) {

                        throw new DuplicateResourceException(
                                        "Aircraft already exists with registration number: "
                                                        + updatedAircraft.getRegistrationNumber());
                }

                aircraft.setRegistrationNumber(
                                updatedAircraft.getRegistrationNumber());

                aircraft.setStatus(
                                updatedAircraft.getStatus());

                aircraft.setAircraftType(
                                updatedAircraft.getAircraftType());

                return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
        }

        @CacheEvict(value = { "aircraft", "flights" }, allEntries = true)
        public AircraftResponseDTO updateAircraft(Long id, AircraftRequestDTO request) {
                Aircraft aircraft = getAircraftEntityById(id);
                if (aircraftRepository.existsByRegistrationNumberAndIdNot(
                                request.getRegistrationNumber(),
                                id)) {

                        throw new DuplicateResourceException(
                                        "Aircraft already exists with registration number: "
                                                        + request.getRegistrationNumber());
                }
                applyRequest(aircraft, request);
                return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
        }

        private Aircraft getAircraftEntityById(Long id) {
                return aircraftRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Aircraft not found with id: " + id));
        }

        private void applyRequest(
                        Aircraft aircraft,
                        AircraftRequestDTO request) {

                aircraft.setRegistrationNumber(
                                request.getRegistrationNumber());

                aircraft.setStatus(
                                request.getStatus());

                aircraft.setAircraftType(
                                aircraftTypeService.findEntityById(
                                                request.getAircraftTypeId()));
        }

        @CacheEvict(value = { "aircraft", "flights" }, allEntries = true)
        public void deleteAircraft(Long id) {
                Aircraft aircraft = getAircraftEntityById(id);

                if (flightRepository.existsByAircraftId(id)) {
                        throw new ResourceInUseException(
                                        "Aircraft cannot be deleted because it is referenced by one or more flights");
                }

                aircraftRepository.delete(aircraft);
        }
}
