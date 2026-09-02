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

import com.airhive.backend.dto.AirportResponseDTO;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.service.AirportService;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirportService airportService;

    private AirportResponseDTO airport;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        airport = new AirportResponseDTO();

        airport.setIataCode("DEL");
        airport.setIcaoCode("VIDP");
        airport.setName("Indira Gandhi International Airport");
        airport.setCity("Delhi");
        airport.setCountry("India");
        airport.setTerminalCount(3);
        airport.setStatus("ACTIVE");
    }

    @Test
    void getAllAirports_shouldReturn200() throws Exception {

        when(airportService.getAllAirports())
                .thenReturn(List.of(airport));

        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iataCode")
                        .value("DEL"))
                .andExpect(jsonPath("$[0].icaoCode")
                        .value("VIDP"))
                .andExpect(jsonPath("$[0].name")
                        .value("Indira Gandhi International Airport"))
                .andExpect(jsonPath("$[0].city")
                        .value("Delhi"))
                .andExpect(jsonPath("$[0].country")
                        .value("India"))
                .andExpect(jsonPath("$[0].terminalCount")
                        .value(3))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"));
    }

    @Test
    void getAirportById_shouldReturn200() throws Exception {

        when(airportService.getAirportById(1L))
                .thenReturn(airport);

        mockMvc.perform(get("/api/airports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.icaoCode")
                        .value("VIDP"))
                .andExpect(jsonPath("$.city")
                        .value("Delhi"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void getAirportById_shouldReturn404_whenNotFound()
            throws Exception {

        when(airportService.getAirportById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Airport not found with id: 999"));

        mockMvc.perform(get("/api/airports/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Airport not found with id: 999"));
    }

    @Test
    void createAirport_shouldReturn201() throws Exception {

        when(airportService.createAirport(any(
                com.airhive.backend.dto.AirportRequestDTO.class)))
                .thenReturn(airport);

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode")
                        .value("DEL"))
                .andExpect(jsonPath("$.icaoCode")
                        .value("VIDP"))
                .andExpect(jsonPath("$.city")
                        .value("Delhi"));
    }

    @Test
    void createAirport_shouldReturn400_whenIataCodeMissing()
            throws Exception {

        String request = """
                {
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenIcaoCodeMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenNameMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenCityMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenCountryMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenTerminalCountMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenTerminalCountNegative()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": -1,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn400_whenStatusMissing()
            throws Exception {

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAirport_shouldReturn409_whenDuplicate()
            throws Exception {

        when(airportService.createAirport(any(
                com.airhive.backend.dto.AirportRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Airport already exists with IATA code: DEL"));

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateAirport_shouldReturn200() throws Exception {

        when(airportService.updateAirport(
                eq(1L),
                any(com.airhive.backend.dto.AirportRequestDTO.class)))
                .thenReturn(airport);

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/airports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode")
                        .value("DEL"));
    }

    @Test
    void updateAirport_shouldReturn400_whenInvalidRequest()
            throws Exception {

        String request = """
                {
                    "iataCode": "",
                    "icaoCode": "",
                    "name": "",
                    "city": "",
                    "country": "",
                    "terminalCount": -1,
                    "status": ""
                }
                """;

        mockMvc.perform(put("/api/airports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAirport_shouldReturn404_whenNotFound()
            throws Exception {

        when(airportService.updateAirport(
                eq(999L),
                any(com.airhive.backend.dto.AirportRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Airport not found with id: 999"));

        String request = """
                {
                    "iataCode": "DEL",
                    "icaoCode": "VIDP",
                    "name": "Indira Gandhi International Airport",
                    "city": "Delhi",
                    "country": "India",
                    "terminalCount": 3,
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/airports/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAirport_shouldReturn204() throws Exception {

        doNothing().when(airportService)
                .deleteAirport(1L);

        mockMvc.perform(delete("/api/airports/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAirport_shouldReturn404_whenNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Airport not found with id: 999"))
                .when(airportService)
                .deleteAirport(999L);

        mockMvc.perform(delete("/api/airports/999"))
                .andExpect(status().isNotFound());
    }
}