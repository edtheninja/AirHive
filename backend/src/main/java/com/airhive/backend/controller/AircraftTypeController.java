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

import com.airhive.backend.entity.AircraftType;
import com.airhive.backend.service.AircraftTypeService;

@RestController
@RequestMapping("/api/aircraft-types")
public class AircraftTypeController {

    private final AircraftTypeService aircraftTypeService;

    public AircraftTypeController(AircraftTypeService aircraftTypeService) {
        this.aircraftTypeService = aircraftTypeService;
    }

    @GetMapping
    public ResponseEntity<List<AircraftType>> getAllAircraftTypes() {
        return ResponseEntity.ok(aircraftTypeService.getAllAircraftTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftType> getAircraftTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(
                aircraftTypeService.getAircraftTypeById(id)
        );
    }

    @PostMapping
    public ResponseEntity<AircraftType> createAircraftType(
            @RequestBody AircraftType aircraftType) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aircraftTypeService.createAircraftType(aircraftType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftType> updateAircraftType(
            @PathVariable Long id,
            @RequestBody AircraftType aircraftType) {

        return ResponseEntity.ok(
                aircraftTypeService.updateAircraftType(id, aircraftType)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraftType(@PathVariable Long id) {

        aircraftTypeService.deleteAircraftType(id);

        return ResponseEntity.noContent().build();
    }
}