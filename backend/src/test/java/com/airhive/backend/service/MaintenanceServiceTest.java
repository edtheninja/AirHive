package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.MaintenanceResponseDTO;
import com.airhive.backend.entity.Maintenance;
import com.airhive.backend.repository.MaintenanceRepository;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(maintenanceRepository);
    }

    @Test
    void getAllMaintenanceReturnsMappedRecords() {
        Maintenance maintenance = new Maintenance();
        maintenance.setOrderNumber("MX-1001");
        maintenance.setType("A-Check");
        maintenance.setSeverity("Routine");
        maintenance.setDue("Scheduled");
        maintenance.setProgress(65);
        maintenance.setEngineer("Alex Morgan");

        when(maintenanceRepository.findAll())
                .thenReturn(List.of(maintenance));

        List<MaintenanceResponseDTO> result =
                maintenanceService.getAllMaintenance();

        assertEquals(1, result.size());
        assertEquals("MX-1001", result.get(0).getOrderNumber());
        assertEquals("A-Check", result.get(0).getType());
        assertEquals("Routine", result.get(0).getSeverity());
        assertEquals("Scheduled", result.get(0).getDue());
        assertEquals(65, result.get(0).getProgress());
        assertEquals("Alex Morgan", result.get(0).getEngineer());

        verify(maintenanceRepository).findAll();
    }

    @Test
    void getAllMaintenanceReturnsEmptyListWhenNoRecordsExist() {
        when(maintenanceRepository.findAll()).thenReturn(List.of());

        List<MaintenanceResponseDTO> result =
                maintenanceService.getAllMaintenance();

        assertEquals(0, result.size());
        verify(maintenanceRepository).findAll();
    }
}
