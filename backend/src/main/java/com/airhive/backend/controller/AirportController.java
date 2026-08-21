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

import com.airhive.backend.dto.AirportRequestDTO;
import com.airhive.backend.dto.AirportResponseDTO;
import com.airhive.backend.mapper.AirportMapper;
import com.airhive.backend.service.AirportService;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping
    public ResponseEntity<List<AirportResponseDTO>> getAllAirports() {

        return ResponseEntity.ok(
                airportService.getAllAirports()
                        .stream()
                        .map(AirportMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponseDTO> getAirportById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                AirportMapper.toResponse(
                        airportService.getAirportById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<AirportResponseDTO> createAirport(
            @RequestBody AirportRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        AirportMapper.toResponse(
                                airportService.createAirport(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportResponseDTO> updateAirport(
            @PathVariable Long id,
            @RequestBody AirportRequestDTO request) {

        return ResponseEntity.ok(
                AirportMapper.toResponse(
                        airportService.updateAirport(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirport(
            @PathVariable Long id) {

        airportService.deleteAirport(id);

        return ResponseEntity.noContent().build();
    }
}