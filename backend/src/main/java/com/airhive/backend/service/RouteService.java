package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.RouteRepository;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    public RouteService(
            RouteRepository routeRepository,
            AirportRepository airportRepository) {

        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + id
                        ));
    }

    public Route createRoute(Route route) {

        validateRoute(route);

        Long departureAirportId =
                route.getDepartureAirport().getId();

        Long arrivalAirportId =
                route.getArrivalAirport().getId();

        if (routeRepository
                .existsByDepartureAirportIdAndArrivalAirportId(
                        departureAirportId,
                        arrivalAirportId)) {

            throw new DuplicateResourceException(
                    "Route already exists from airport "
                            + departureAirportId
                            + " to airport "
                            + arrivalAirportId
            );
        }

        Airport departureAirport =
                airportRepository.findById(departureAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Departure airport not found with id: "
                                                + departureAirportId
                                ));

        Airport arrivalAirport =
                airportRepository.findById(arrivalAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Arrival airport not found with id: "
                                                + arrivalAirportId
                                ));

        route.setDepartureAirport(departureAirport);
        route.setArrivalAirport(arrivalAirport);

        return routeRepository.save(route);
    }

    public Route updateRoute(Long id, Route updatedRoute) {

        Route existingRoute = getRouteById(id);

        validateRoute(updatedRoute);

        Long departureAirportId =
                updatedRoute.getDepartureAirport().getId();

        Long arrivalAirportId =
                updatedRoute.getArrivalAirport().getId();

        if (!existingRoute.getDepartureAirport().getId()
                .equals(departureAirportId)
                || !existingRoute.getArrivalAirport().getId()
                .equals(arrivalAirportId)) {

            if (routeRepository
                    .existsByDepartureAirportIdAndArrivalAirportId(
                            departureAirportId,
                            arrivalAirportId)) {

                throw new DuplicateResourceException(
                        "Route already exists from airport "
                                + departureAirportId
                                + " to airport "
                                + arrivalAirportId
                );
            }
        }

        Airport departureAirport =
                airportRepository.findById(departureAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Departure airport not found with id: "
                                                + departureAirportId
                                ));

        Airport arrivalAirport =
                airportRepository.findById(arrivalAirportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Arrival airport not found with id: "
                                                + arrivalAirportId
                                ));

        existingRoute.setDepartureAirport(departureAirport);
        existingRoute.setArrivalAirport(arrivalAirport);
        existingRoute.setDistanceKm(
                updatedRoute.getDistanceKm()
        );
        existingRoute.setEstimatedDurationMinutes(
                updatedRoute.getEstimatedDurationMinutes()
        );
        existingRoute.setStatus(
                updatedRoute.getStatus()
        );

        return routeRepository.save(existingRoute);
    }

    public void deleteRoute(Long id) {
        Route route = getRouteById(id);
        routeRepository.delete(route);
    }

    private void validateRoute(Route route) {

        if (route.getDepartureAirport() == null
                || route.getDepartureAirport().getId() == null) {

            throw new RuntimeException(
                    "Departure airport is required"
            );
        }

        if (route.getArrivalAirport() == null
                || route.getArrivalAirport().getId() == null) {

            throw new RuntimeException(
                    "Arrival airport is required"
            );
        }

        if (route.getDepartureAirport().getId()
                .equals(route.getArrivalAirport().getId())) {

            throw new RuntimeException(
                    "Departure and arrival airports cannot be the same"
            );
        }

        if (route.getDistanceKm() == null
                || route.getDistanceKm() <= 0) {

            throw new RuntimeException(
                    "Distance must be greater than zero"
            );
        }

        if (route.getEstimatedDurationMinutes() == null
                || route.getEstimatedDurationMinutes() <= 0) {

            throw new RuntimeException(
                    "Estimated duration must be greater than zero"
            );
        }

        if (route.getStatus() == null
                || route.getStatus().isBlank()) {

            throw new RuntimeException(
                    "Route status is required"
            );
        }
    }
}