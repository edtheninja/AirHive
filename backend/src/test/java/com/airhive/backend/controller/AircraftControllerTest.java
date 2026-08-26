package com.airhive.backend.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.service.AircraftService;

@WebMvcTest(AircraftController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AircraftService aircraftService;

    private Aircraft aircraft;
    private AircraftType aircraftType;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        aircraftType = new AircraftType();
        aircraftType.setTypeCode("A320");

        aircraft = new Aircraft();
        aircraft.setRegistrationNumber("VT-AIR01");
        aircraft.setAircraftType(aircraftType);
        aircraft.setStatus("ACTIVE");
    }

    // ALL YOUR EXISTING @Test METHODS CONTINUE HERE

    @Test
    void getAllAircraft_shouldReturn200() throws Exception {

        when(aircraftService.getAllAircraft())
                .thenReturn(List.of(aircraft));

        mockMvc.perform(get("/api/aircraft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$[0].aircraftTypeCode")
                        .value("A320"));
    }

    @Test
    void getAircraftById_shouldReturn200() throws Exception {

        when(aircraftService.getAircraftById(1L))
                .thenReturn(aircraft);

        mockMvc.perform(get("/api/aircraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void getAircraftById_shouldReturn404_whenNotFound()
            throws Exception {

        when(aircraftService.getAircraftById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Aircraft not found with id: 999"));

        mockMvc.perform(get("/api/aircraft/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Aircraft not found with id: 999"));
    }

    @Test
    void createAircraft_shouldReturn201() throws Exception {

        when(aircraftService.createAircraft(any(
                com.airhive.backend.dto.AircraftRequestDTO.class)))
                .thenReturn(aircraft);

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber")
                        .value("VT-AIR01"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void createAircraft_shouldReturn400_whenRegistrationNumberMissing()
            throws Exception {

        String request = """
                {
                    "aircraftTypeId": 1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraft_shouldReturn400_whenAircraftTypeIdMissing()
            throws Exception {

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraft_shouldReturn400_whenAircraftTypeIdNotPositive()
            throws Exception {

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 0,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraft_shouldReturn400_whenStatusMissing()
            throws Exception {

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 1
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraft_shouldReturn409_whenDuplicate()
            throws Exception {

        when(aircraftService.createAircraft(any(
                com.airhive.backend.dto.AircraftRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Aircraft already exists with registration number: VT-AIR01"));

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateAircraft_shouldReturn200() throws Exception {

        when(aircraftService.updateAircraft(
                eq(1L),
                any(com.airhive.backend.dto.AircraftRequestDTO.class)))
                .thenReturn(aircraft);

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/aircraft/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber")
                        .value("VT-AIR01"));
    }

    @Test
    void updateAircraft_shouldReturn400_whenInvalidRequest()
            throws Exception {

        String request = """
                {
                    "registrationNumber": "",
                    "aircraftTypeId": null,
                    "status": ""
                }
                """;

        mockMvc.perform(put("/api/aircraft/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAircraft_shouldReturn404_whenNotFound()
            throws Exception {

        when(aircraftService.updateAircraft(
                eq(999L),
                any(com.airhive.backend.dto.AircraftRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Aircraft not found with id: 999"));

        String request = """
                {
                    "registrationNumber": "VT-AIR01",
                    "aircraftTypeId": 1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/aircraft/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAircraft_shouldReturn204() throws Exception {

        doNothing().when(aircraftService).deleteAircraft(1L);

        mockMvc.perform(delete("/api/aircraft/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAircraft_shouldReturn404_whenNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Aircraft not found with id: 999"))
                .when(aircraftService)
                .deleteAircraft(999L);

        mockMvc.perform(delete("/api/aircraft/999"))
                .andExpect(status().isNotFound());
    }
}