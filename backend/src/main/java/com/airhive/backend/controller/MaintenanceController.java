package com.airhive.backend.controller;

import com.airhive.backend.dto.MaintenanceResponseDTO;
import com.airhive.backend.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceResponseDTO>> getAllMaintenance() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenance());
    }
}
