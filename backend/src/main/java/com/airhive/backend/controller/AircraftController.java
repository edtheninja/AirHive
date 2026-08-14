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

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.service.AircraftService;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @GetMapping
    public ResponseEntity<List<Aircraft>> getAllAircraft() {
        return ResponseEntity.ok(aircraftService.getAllAircraft());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aircraft> getAircraftById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                aircraftService.getAircraftById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Aircraft> createAircraft(
            @RequestBody Aircraft aircraft) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aircraftService.createAircraft(aircraft));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aircraft> updateAircraft(
            @PathVariable Long id,
            @RequestBody Aircraft aircraft) {

        return ResponseEntity.ok(
                aircraftService.updateAircraft(id, aircraft)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(
            @PathVariable Long id) {

        aircraftService.deleteAircraft(id);

        return ResponseEntity.noContent().build();
    }
}