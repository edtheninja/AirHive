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

import com.airhive.backend.dto.AircraftTypeRequestDTO;
import com.airhive.backend.dto.AircraftTypeResponseDTO;
import com.airhive.backend.mapper.AircraftTypeMapper;
import com.airhive.backend.service.AircraftTypeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/aircraft-types")
public class AircraftTypeController {

    private final AircraftTypeService aircraftTypeService;

    public AircraftTypeController(
            AircraftTypeService aircraftTypeService) {

        this.aircraftTypeService = aircraftTypeService;
    }

    @GetMapping
    public ResponseEntity<List<AircraftTypeResponseDTO>>
            getAllAircraftTypes() {

        return ResponseEntity.ok(
                aircraftTypeService.getAllAircraftTypes()
                        .stream()
                        .map(AircraftTypeMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftTypeResponseDTO>
            getAircraftTypeById(
                    @PathVariable Long id) {

        return ResponseEntity.ok(
                AircraftTypeMapper.toResponse(
                        aircraftTypeService
                                .getAircraftTypeById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<AircraftTypeResponseDTO>
            createAircraftType(
                    @Valid @RequestBody AircraftTypeRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        AircraftTypeMapper.toResponse(
                                aircraftTypeService
                                        .createAircraftType(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftTypeResponseDTO>
            updateAircraftType(
                    @PathVariable Long id,
                    @Valid @RequestBody AircraftTypeRequestDTO request) {

        return ResponseEntity.ok(
                AircraftTypeMapper.toResponse(
                        aircraftTypeService
                                .updateAircraftType(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraftType(
            @PathVariable Long id) {

        aircraftTypeService.deleteAircraftType(id);

        return ResponseEntity.noContent().build();
    }
}