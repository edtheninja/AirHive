package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AircraftRequestDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftRepository;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftTypeService aircraftTypeService;

    public AircraftService(
            AircraftRepository aircraftRepository,
            AircraftTypeService aircraftTypeService) {
        this.aircraftRepository = aircraftRepository;
        this.aircraftTypeService = aircraftTypeService;
    }

    public List<Aircraft> getAllAircraft() {
        return aircraftRepository.findAll();
    }

    public Aircraft getAircraftById(Long id) {
        return aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft not found with id: " + id));
    }

    public Aircraft createAircraft(Aircraft aircraft) {
        if (aircraftRepository.existsByRegistrationNumber(
                aircraft.getRegistrationNumber())) {

            throw new DuplicateResourceException(
                "Aircraft already exists with registration number: "
                + aircraft.getRegistrationNumber());
            }

        return aircraftRepository.save(aircraft);
    }

    public Aircraft createAircraft(AircraftRequestDTO request) {
        Aircraft aircraft = new Aircraft();
        applyRequest(aircraft, request);
        return createAircraft(aircraft);
    }

    public Aircraft updateAircraft(Long id, Aircraft updatedAircraft) {

        Aircraft aircraft = getAircraftById(id);

        aircraft.setRegistrationNumber(
                updatedAircraft.getRegistrationNumber());

        aircraft.setStatus(
                updatedAircraft.getStatus());

        aircraft.setAircraftType(
                updatedAircraft.getAircraftType());

        return aircraftRepository.save(aircraft);
    }

    public Aircraft updateAircraft(Long id, AircraftRequestDTO request) {
        Aircraft aircraft = getAircraftById(id);
        applyRequest(aircraft, request);
        return aircraftRepository.save(aircraft);
    }

    private void applyRequest(Aircraft aircraft, AircraftRequestDTO request) {
        aircraft.setRegistrationNumber(request.getRegistrationNumber());
        aircraft.setStatus(request.getStatus());
        aircraft.setAircraftType(
                aircraftTypeService.getAircraftTypeById(request.getAircraftTypeId()));
    }

    public void deleteAircraft(Long id) {
        Aircraft aircraft = getAircraftById(id);
        aircraftRepository.delete(aircraft);
    }
}