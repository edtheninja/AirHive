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

import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.service.AircraftTypeService;

@WebMvcTest(AircraftTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AircraftTypeService aircraftTypeService;

    private AircraftTypeResponseDTO aircraftType;

    @BeforeEach
    void setUp() {

        aircraftType = new AircraftTypeResponseDTO();

        aircraftType.setTypeCode("A320");
        aircraftType.setManufacturer("Airbus");
        aircraftType.setModel("A320-200");
        aircraftType.setPassengerCapacity(180);
        aircraftType.setCrewCapacity(6);
    }

    @Test
    void getAllAircraftTypes_shouldReturn200() throws Exception {

        when(aircraftTypeService.getAllAircraftTypes())
                .thenReturn(new java.util.ArrayList<>(List.of(aircraftType)));

        mockMvc.perform(get("/api/aircraft-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].typeCode")
                        .value("A320"))
                .andExpect(jsonPath("$[0].manufacturer")
                        .value("Airbus"))
                .andExpect(jsonPath("$[0].model")
                        .value("A320-200"))
                .andExpect(jsonPath("$[0].passengerCapacity")
                        .value(180))
                .andExpect(jsonPath("$[0].crewCapacity")
                        .value(6));
    }

    @Test
    void getAircraftTypeById_shouldReturn200() throws Exception {

        when(aircraftTypeService.getAircraftTypeById(1L))
                .thenReturn(aircraftType);

        mockMvc.perform(get("/api/aircraft-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode")
                        .value("A320"))
                .andExpect(jsonPath("$.manufacturer")
                        .value("Airbus"))
                .andExpect(jsonPath("$.model")
                        .value("A320-200"));
    }

    @Test
    void getAircraftTypeById_shouldReturn404_whenNotFound()
            throws Exception {

        when(aircraftTypeService.getAircraftTypeById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Aircraft type not found with id: 999"));

        mockMvc.perform(get("/api/aircraft-types/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Aircraft type not found with id: 999"));
    }

    @Test
    void createAircraftType_shouldReturn201() throws Exception {

        when(aircraftTypeService.createAircraftType(any(
                com.airhive.backend.dto.AircraftTypeRequestDTO.class)))
                .thenReturn(aircraftType);

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCode")
                        .value("A320"))
                .andExpect(jsonPath("$.manufacturer")
                        .value("Airbus"))
                .andExpect(jsonPath("$.model")
                        .value("A320-200"));
    }

    @Test
    void createAircraftType_shouldReturn400_whenTypeCodeMissing()
            throws Exception {

        String request = """
                {
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenManufacturerMissing()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenModelMissing()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenPassengerCapacityMissing()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenPassengerCapacityNotPositive()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 0,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenCrewCapacityMissing()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn400_whenCrewCapacityNotPositive()
            throws Exception {

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 0
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAircraftType_shouldReturn409_whenDuplicate()
            throws Exception {

        when(aircraftTypeService.createAircraftType(any(
                com.airhive.backend.dto.AircraftTypeRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Aircraft type already exists with type code: A320"));

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(post("/api/aircraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateAircraftType_shouldReturn200() throws Exception {

        when(aircraftTypeService.updateAircraftType(
                eq(1L),
                any(com.airhive.backend.dto.AircraftTypeRequestDTO.class)))
                .thenReturn(aircraftType);

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(put("/api/aircraft-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode")
                        .value("A320"));
    }

    @Test
    void updateAircraftType_shouldReturn400_whenInvalidRequest()
            throws Exception {

        String request = """
                {
                    "typeCode": "",
                    "manufacturer": "",
                    "model": "",
                    "passengerCapacity": 0,
                    "crewCapacity": 0
                }
                """;

        mockMvc.perform(put("/api/aircraft-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAircraftType_shouldReturn404_whenNotFound()
            throws Exception {

        when(aircraftTypeService.updateAircraftType(
                eq(999L),
                any(com.airhive.backend.dto.AircraftTypeRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Aircraft type not found with id: 999"));

        String request = """
                {
                    "typeCode": "A320",
                    "manufacturer": "Airbus",
                    "model": "A320-200",
                    "passengerCapacity": 180,
                    "crewCapacity": 6
                }
                """;

        mockMvc.perform(put("/api/aircraft-types/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAircraftType_shouldReturn204() throws Exception {

        doNothing().when(aircraftTypeService)
                .deleteAircraftType(1L);

        mockMvc.perform(delete("/api/aircraft-types/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAircraftType_shouldReturn404_whenNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Aircraft type not found with id: 999"))
                .when(aircraftTypeService)
                .deleteAircraftType(999L);

        mockMvc.perform(delete("/api/aircraft-types/999"))
                .andExpect(status().isNotFound());
    }
}