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

import com.airhive.backend.dto.RouteRequestDTO;
import com.airhive.backend.dto.RouteResponseDTO;
import com.airhive.backend.mapper.RouteMapper;
import com.airhive.backend.service.RouteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteResponseDTO>> getAllRoutes() {

        return ResponseEntity.ok(
                routeService.getAllRoutes()
                        .stream()
                        .map(RouteMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getRouteById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                RouteMapper.toResponse(
                        routeService.getRouteById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<RouteResponseDTO> createRoute(
            @Valid @RequestBody RouteRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        RouteMapper.toResponse(
                                routeService.createRoute(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequestDTO request) {

        return ResponseEntity.ok(
                RouteMapper.toResponse(
                        routeService.updateRoute(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id) {

        routeService.deleteRoute(id);

        return ResponseEntity.noContent().build();
    }
}