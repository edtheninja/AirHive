package com.airhive.backend.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.AircraftTypeMapper;
import com.airhive.backend.repository.AircraftTypeRepository;

@Service
public class AircraftTypeService {

    private final AircraftTypeRepository aircraftTypeRepository;

    public AircraftTypeService(
            AircraftTypeRepository aircraftTypeRepository) {

        this.aircraftTypeRepository = aircraftTypeRepository;
    }

    @Cacheable("aircraftTypes")
    public List<AircraftTypeResponseDTO> getAllAircraftTypes() {

        return aircraftTypeRepository.findAll()
                .stream()
                .map(AircraftTypeMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "aircraftTypes", key = "#id")
    public AircraftTypeResponseDTO getAircraftTypeById(Long id) {

        return AircraftTypeMapper.toResponse(findEntityById(id));
    }

    public AircraftType findEntityById(Long id) {

        return aircraftTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft type not found with id: " + id));
    }

    @CacheEvict(value = { "aircraftTypes", "aircraft" }, allEntries = true)
    public AircraftTypeResponseDTO createAircraftType(
            AircraftTypeRequestDTO request) {

        AircraftType aircraftType = new AircraftType();

        applyRequest(aircraftType, request);

        return AircraftTypeMapper.toResponse(
                aircraftTypeRepository.save(aircraftType));
    }

    @CacheEvict(value = { "aircraftTypes", "aircraft" }, allEntries = true)
    public AircraftTypeResponseDTO updateAircraftType(
            Long id,
            AircraftTypeRequestDTO request) {

        AircraftType existing = findEntityById(id);

        applyRequest(existing, request);

        return AircraftTypeMapper.toResponse(
                aircraftTypeRepository.save(existing));
    }

    @CacheEvict(value = { "aircraftTypes", "aircraft" }, allEntries = true)
    public void deleteAircraftType(Long id) {

        aircraftTypeRepository.delete(findEntityById(id));
    }

    private void applyRequest(
            AircraftType aircraftType,
            AircraftTypeRequestDTO request) {

        aircraftType.setTypeCode(request.getTypeCode());
        aircraftType.setManufacturer(request.getManufacturer());
        aircraftType.setModel(request.getModel());
        aircraftType.setPassengerCapacity(request.getPassengerCapacity());
        aircraftType.setCrewCapacity(request.getCrewCapacity());
    }
}
