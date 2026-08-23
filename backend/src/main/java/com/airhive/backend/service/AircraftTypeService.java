package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AircraftTypeRepository;

@Service
public class AircraftTypeService {

    private final AircraftTypeRepository aircraftTypeRepository;

    public AircraftTypeService(
            AircraftTypeRepository aircraftTypeRepository) {

        this.aircraftTypeRepository = aircraftTypeRepository;
    }

    public List<AircraftType> getAllAircraftTypes() {

        return aircraftTypeRepository.findAll();
    }

    public AircraftType getAircraftTypeById(Long id) {

        return aircraftTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft type not found with id: " + id));
    }

    public AircraftType createAircraftType(
            AircraftTypeRequestDTO request) {

        AircraftType aircraftType = new AircraftType();

        applyRequest(aircraftType, request);

        return aircraftTypeRepository.save(aircraftType);
    }

    public AircraftType updateAircraftType(
            Long id,
            AircraftTypeRequestDTO request) {

        AircraftType existing = getAircraftTypeById(id);

        applyRequest(existing, request);

        return aircraftTypeRepository.save(existing);
    }

    public void deleteAircraftType(Long id) {

        AircraftType existing = getAircraftTypeById(id);

        aircraftTypeRepository.delete(existing);
    }

    private void applyRequest(
            AircraftType aircraftType,
            AircraftTypeRequestDTO request) {

        aircraftType.setTypeCode(
                request.getTypeCode());

        aircraftType.setManufacturer(
                request.getManufacturer());

        aircraftType.setModel(
                request.getModel());

        aircraftType.setPassengerCapacity(
                request.getPassengerCapacity());

        aircraftType.setCrewCapacity(
                request.getCrewCapacity());
    }
}