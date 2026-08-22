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

import com.airhive.backend.dto.AircraftRequestDTO;
import com.airhive.backend.dto.AircraftResponseDTO;
import com.airhive.backend.mapper.AircraftMapper;
import com.airhive.backend.service.AircraftService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @GetMapping
    public ResponseEntity<List<AircraftResponseDTO>> getAllAircraft() {

        return ResponseEntity.ok(
                aircraftService.getAllAircraft()
                        .stream()
                        .map(AircraftMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponseDTO> getAircraftById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                AircraftMapper.toResponse(
                        aircraftService.getAircraftById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<AircraftResponseDTO> createAircraft(
            @Valid @RequestBody AircraftRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        AircraftMapper.toResponse(
                                aircraftService.createAircraft(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponseDTO> updateAircraft(
            @PathVariable Long id,
            @Valid @RequestBody AircraftRequestDTO request) {

        return ResponseEntity.ok(
                AircraftMapper.toResponse(
                        aircraftService.updateAircraft(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(
            @PathVariable Long id) {

        aircraftService.deleteAircraft(id);

        return ResponseEntity.noContent().build();
    }
}