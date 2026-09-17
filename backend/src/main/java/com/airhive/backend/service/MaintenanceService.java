package com.airhive.backend.service;

import com.airhive.backend.dto.MaintenanceResponseDTO;
import com.airhive.backend.mapper.MaintenanceMapper;
import com.airhive.backend.repository.MaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> getAllMaintenance() {
        return maintenanceRepository.findAll()
                .stream()
                .map(MaintenanceMapper::toResponse)
                .toList();
    }
}
