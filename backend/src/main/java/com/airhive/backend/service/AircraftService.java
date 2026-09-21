package com.airhive.backend.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        private static final Set<String> VALID_AIRCRAFT_STATUSES = Set.of(
                        "ACTIVE",
                        "INACTIVE");

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

                String newStatus =
                normalizeAircraftStatus(updatedAircraft.getStatus());
                validateAircraftTypeUpdate(
                                id,
                                updatedAircraft.getAircraftType().getId(),
                                aircraft);

                aircraft.setRegistrationNumber(
                                updatedAircraft.getRegistrationNumber());

                aircraft.setStatus(newStatus);

                aircraft.setAircraftType(
        aircraftTypeService.findEntityById(
                updatedAircraft.getAircraftType().getId()));

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

                String newStatus =
                normalizeAircraftStatus(request.getStatus());

                validateAircraftTypeUpdate(
                                id,
                                request.getAircraftTypeId(),
                                aircraft);

                applyRequest(aircraft, request);

                return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
        }

        private void validateAircraftTypeUpdate(
                        Long aircraftId,
                        Long requestedAircraftTypeId,
                        Aircraft existingAircraft) {

                Long existingAircraftTypeId = existingAircraft.getAircraftType().getId();

                if (!Objects.equals(existingAircraftTypeId, requestedAircraftTypeId)
                                && flightRepository.existsByAircraftId(aircraftId)) {

                        throw new ResourceInUseException(
                                        "Aircraft type cannot be changed because the aircraft is referenced by one or more flights");
                }
        }

        private Aircraft getAircraftEntityById(Long id) {
                return aircraftRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Aircraft not found with id: " + id));
        }

        private String normalizeAircraftStatus(String status) {

        if (status == null || status.isBlank()) {
                throw new IllegalArgumentException(
                                "Aircraft status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!VALID_AIRCRAFT_STATUSES.contains(normalizedStatus)) {
                throw new IllegalArgumentException(
                                "Invalid aircraft status: " + status);
        }

        return normalizedStatus;
}

        private void applyRequest(
                Aircraft aircraft,
                AircraftRequestDTO request) {

        String newStatus =
                normalizeAircraftStatus(request.getStatus());
        aircraft.setRegistrationNumber(
                        request.getRegistrationNumber());

        aircraft.setStatus(newStatus);

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
