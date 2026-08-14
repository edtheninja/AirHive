package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.repository.AircraftTypeRepository;

@Service
public class AircraftTypeService {

    private final AircraftTypeRepository aircraftTypeRepository;

    public AircraftTypeService(AircraftTypeRepository aircraftTypeRepository) {
        this.aircraftTypeRepository = aircraftTypeRepository;
    }

    public List<AircraftType> getAllAircraftTypes() {
        return aircraftTypeRepository.findAll();
    }

    public AircraftType getAircraftTypeById(Long id) {
        return aircraftTypeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Aircraft type not found with id: " + id));
    }

    public AircraftType createAircraftType(AircraftType aircraftType) {
        return aircraftTypeRepository.save(aircraftType);
    }

    public AircraftType updateAircraftType(Long id, AircraftType aircraftType) {
        AircraftType existing = getAircraftTypeById(id);

        existing.setTypeCode(aircraftType.getTypeCode());
        existing.setManufacturer(aircraftType.getManufacturer());
        existing.setModel(aircraftType.getModel());
        existing.setPassengerCapacity(aircraftType.getPassengerCapacity());
        existing.setCrewCapacity(aircraftType.getCrewCapacity());

        return aircraftTypeRepository.save(existing);
    }

    public void deleteAircraftType(Long id) {
        AircraftType existing = getAircraftTypeById(id);
        aircraftTypeRepository.delete(existing);
    }
}