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

import com.airhive.backend.entity.Route;
import com.airhive.backend.service.RouteService;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(
                routeService.getAllRoutes()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                routeService.getRouteById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Route> createRoute(
            @RequestBody Route route) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(routeService.createRoute(route));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(
            @PathVariable Long id,
            @RequestBody Route route) {

        return ResponseEntity.ok(
                routeService.updateRoute(id, route)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id) {

        routeService.deleteRoute(id);

        return ResponseEntity.noContent().build();
    }
}