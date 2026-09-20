package com.airhive.backend.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.ResourceInUseException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.AircraftTypeMapper;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.AircraftTypeRepository;

@Service
public class AircraftTypeService {

    private final AircraftTypeRepository aircraftTypeRepository;
    private final AircraftRepository aircraftRepository;

    public AircraftTypeService(
            AircraftTypeRepository aircraftTypeRepository,
            AircraftRepository aircraftRepository) {

        this.aircraftTypeRepository = aircraftTypeRepository;
        this.aircraftRepository = aircraftRepository;
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

    if (aircraftTypeRepository.existsByTypeCodeIgnoreCase(
            request.getTypeCode())) {

        throw new IllegalArgumentException(
                "Aircraft type already exists with type code: "
                        + request.getTypeCode());
    }

    applyRequest(aircraftType, request);

    return AircraftTypeMapper.toResponse(
            aircraftTypeRepository.save(aircraftType));
}

    @CacheEvict(value = { "aircraftTypes", "aircraft" }, allEntries = true)
    public AircraftTypeResponseDTO updateAircraftType(
        Long id,
        AircraftTypeRequestDTO request) {

    AircraftType existing = findEntityById(id);

    if (aircraftTypeRepository.existsByTypeCodeIgnoreCaseAndIdNot(
            request.getTypeCode(),
            id)) {

        throw new IllegalArgumentException(
                "Aircraft type already exists with type code: "
                        + request.getTypeCode());
    }

    applyRequest(existing, request);

    return AircraftTypeMapper.toResponse(
            aircraftTypeRepository.save(existing));
}

    @CacheEvict(value = { "aircraftTypes", "aircraft" }, allEntries = true)
    public void deleteAircraftType(Long id) {

        AircraftType aircraftType = findEntityById(id);

        if (aircraftRepository.existsByAircraftTypeId(id)) {
            throw new ResourceInUseException(
                    "Aircraft type cannot be deleted because it is referenced by one or more aircraft");
        }

        aircraftTypeRepository.delete(aircraftType);
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
