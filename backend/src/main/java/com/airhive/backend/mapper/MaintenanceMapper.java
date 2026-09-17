package com.airhive.backend.mapper;

import com.airhive.backend.dto.MaintenanceResponseDTO;
import com.airhive.backend.entity.Maintenance;

public final class MaintenanceMapper {

    private MaintenanceMapper() {
    }

    public static MaintenanceResponseDTO toResponse(Maintenance maintenance) {
        MaintenanceResponseDTO response = new MaintenanceResponseDTO();

        response.setId(maintenance.getId());
        response.setOrderNumber(maintenance.getOrderNumber());
        response.setAircraft(maintenance.getAircraft());
        response.setType(maintenance.getType());
        response.setSeverity(maintenance.getSeverity());
        response.setDue(maintenance.getDue());
        response.setProgress(maintenance.getProgress());
        response.setEngineer(maintenance.getEngineer());

        return response;
    }
}
