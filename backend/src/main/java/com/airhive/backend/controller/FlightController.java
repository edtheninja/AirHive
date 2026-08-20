package com.airhive.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airhive.backend.dto.FlightRequestDTO;
import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.mapper.FlightMapper;
import com.airhive.backend.service.FlightService;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<FlightResponseDTO>> getAllFlights() {

        return ResponseEntity.ok(
                flightService.getAllFlights()
                        .stream()
                        .map(FlightMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponseDTO> getFlightById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                FlightMapper.toResponse(
                        flightService.getFlightById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<FlightResponseDTO> createFlight(
            @RequestBody FlightRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        FlightMapper.toResponse(
                                flightService.createFlight(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponseDTO> updateFlight(
            @PathVariable Long id,
            @RequestBody FlightRequestDTO request) {

        return ResponseEntity.ok(
                FlightMapper.toResponse(
                        flightService.updateFlight(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(
            @PathVariable Long id) {

        flightService.deleteFlight(id);

        return ResponseEntity.noContent().build();
    }
}