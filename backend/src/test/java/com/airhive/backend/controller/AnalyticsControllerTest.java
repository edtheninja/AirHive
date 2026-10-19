
package com.airhive.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.ActivityCountDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.AircraftUtilizationDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.HistoricalActivityDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.StatusCountDTO;
import com.airhive.backend.service.AnalyticsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void getOverview_shouldReturn200WithAnalyticsData() throws Exception {
        AnalyticsOverviewResponseDTO overview =
                new AnalyticsOverviewResponseDTO();

        overview.setTotalFlights(10);
        overview.setDelayedFlights(2);
        overview.setCancelledFlights(1);
        overview.setAirborneFlights(3);

        overview.setTotalAircraft(5);
        overview.setActiveAircraft(3);
        overview.setInactiveAircraft(1);
        overview.setMaintenanceAircraft(1);

        overview.setFlightStatusDistribution(List.of(
                new StatusCountDTO("SCHEDULED", 4),
                new StatusCountDTO("DELAYED", 2)));

        overview.setAircraftUtilization(List.of(
                new AircraftUtilizationDTO(
                        1L, "VT-AIR01", "A320", "ACTIVE", 4)));

        overview.setAirportActivity(List.of(
                new ActivityCountDTO("DEL", 3, 1, 4)));

        overview.setRouteActivity(List.of(
                new ActivityCountDTO("DEL → BOM", 0, 0, 3)));

        overview.setHistoricalActivity(List.of(
                new HistoricalActivityDTO("2026-10-18", 5, 1, 0)));

        when(analyticsService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFlights").value(10))
                .andExpect(jsonPath("$.delayedFlights").value(2))
                .andExpect(jsonPath("$.cancelledFlights").value(1))
                .andExpect(jsonPath("$.airborneFlights").value(3))
                .andExpect(jsonPath("$.totalAircraft").value(5))
                .andExpect(jsonPath("$.activeAircraft").value(3))
                .andExpect(jsonPath("$.inactiveAircraft").value(1))
                .andExpect(jsonPath("$.maintenanceAircraft").value(1))
                .andExpect(jsonPath("$.flightStatusDistribution[0].status")
                        .value("SCHEDULED"))
                .andExpect(jsonPath("$.flightStatusDistribution[0].count")
                        .value(4))
                .andExpect(jsonPath("$.aircraftUtilization[0].registrationNumber")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$.aircraftUtilization[0].assignedFlights")
                        .value(4))
                .andExpect(jsonPath("$.airportActivity[0].name")
                        .value("DEL"))
                .andExpect(jsonPath("$.airportActivity[0].total")
                        .value(4))
                .andExpect(jsonPath("$.routeActivity[0].name")
                        .value("DEL → BOM"))
                .andExpect(jsonPath("$.routeActivity[0].total")
                        .value(3))
                .andExpect(jsonPath("$.historicalActivity[0].date")
                        .value("2026-10-18"))
                .andExpect(jsonPath("$.historicalActivity[0].total")
                        .value(5))
                .andExpect(jsonPath("$.historicalActivity[0].delayed")
                        .value(1));

        verify(analyticsService).getOverview();
    }

    @Test
    void getOverview_shouldReturn200WithZeroCountsAndEmptyLists()
            throws Exception {
        AnalyticsOverviewResponseDTO overview =
                new AnalyticsOverviewResponseDTO();

        overview.setFlightStatusDistribution(List.of());
        overview.setAircraftUtilization(List.of());
        overview.setAirportActivity(List.of());
        overview.setRouteActivity(List.of());
        overview.setHistoricalActivity(List.of());

        when(analyticsService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFlights").value(0))
                .andExpect(jsonPath("$.totalAircraft").value(0))
                .andExpect(jsonPath("$.flightStatusDistribution").isEmpty())
                .andExpect(jsonPath("$.aircraftUtilization").isEmpty())
                .andExpect(jsonPath("$.airportActivity").isEmpty())
                .andExpect(jsonPath("$.routeActivity").isEmpty())
                .andExpect(jsonPath("$.historicalActivity").isEmpty());

        verify(analyticsService).getOverview();
    }
}
