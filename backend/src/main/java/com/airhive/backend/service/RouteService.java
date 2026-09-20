package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.dto.RouteRequestDTO;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.entity.Route;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceInUseException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;

@Service
public class RouteService {

        private final RouteRepository routeRepository;
        private final AirportRepository airportRepository;
        private final FlightRepository flightRepository;

        public RouteService(
                        RouteRepository routeRepository,
                        AirportRepository airportRepository,
                        FlightRepository flightRepository) {

                this.routeRepository = routeRepository;
                this.airportRepository = airportRepository;
                this.flightRepository = flightRepository;
        }

        public List<Route> getAllRoutes() {
                return routeRepository.findAll();
        }

        public Route getRouteById(Long id) {

                return routeRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with id: " + id));
        }

        public Route createRoute(RouteRequestDTO request) {

                validateRoute(request);

                Long departureAirportId = request.getDepartureAirportId();

                Long arrivalAirportId = request.getArrivalAirportId();

                if (routeRepository
                                .existsByDepartureAirportIdAndArrivalAirportId(
                                                departureAirportId,
                                                arrivalAirportId)) {

                        throw new DuplicateResourceException(
                                        "Route already exists from airport "
                                                        + departureAirportId
                                                        + " to airport "
                                                        + arrivalAirportId);
                }

                Airport departureAirport = airportRepository.findById(departureAirportId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Departure airport not found with id: "
                                                                + departureAirportId));

                Airport arrivalAirport = airportRepository.findById(arrivalAirportId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Arrival airport not found with id: "
                                                                + arrivalAirportId));

                Route route = new Route();

                route.setDepartureAirport(departureAirport);
                route.setArrivalAirport(arrivalAirport);
                route.setDistanceKm(request.getDistanceKm());
                route.setEstimatedDurationMinutes(
                                request.getEstimatedDurationMinutes());
                route.setStatus(request.getStatus());

                return routeRepository.save(route);
        }

        public Route updateRoute(
                        Long id,
                        RouteRequestDTO request) {

                Route existingRoute = getRouteById(id);

                validateRoute(request);

                boolean airportsChanged = !existingRoute.getDepartureAirport().getId()
                                .equals(request.getDepartureAirportId())
                                || !existingRoute.getArrivalAirport().getId()
                                                .equals(request.getArrivalAirportId());

                if (airportsChanged
                                && flightRepository.existsByRouteId(id)) {

                        throw new ResourceInUseException(
                                        "Route airports cannot be changed because the route is referenced by one or more flights");
                }

                Long departureAirportId = request.getDepartureAirportId();

                Long arrivalAirportId = request.getArrivalAirportId();

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
                                                                + arrivalAirportId);
                        }
                }

                Airport departureAirport = airportRepository.findById(departureAirportId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Departure airport not found with id: "
                                                                + departureAirportId));

                Airport arrivalAirport = airportRepository.findById(arrivalAirportId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Arrival airport not found with id: "
                                                                + arrivalAirportId));

                existingRoute.setDepartureAirport(
                                departureAirport);

                existingRoute.setArrivalAirport(
                                arrivalAirport);

                existingRoute.setDistanceKm(
                                request.getDistanceKm());

                existingRoute.setEstimatedDurationMinutes(
                                request.getEstimatedDurationMinutes());

                existingRoute.setStatus(
                                request.getStatus());

                return routeRepository.save(existingRoute);
        }

        public void deleteRoute(Long id) {

                Route route = getRouteById(id);

                if (flightRepository.existsByRouteId(id)) {
                        throw new ResourceInUseException(
                                        "Route cannot be deleted because it is referenced by one or more flights");
                }

                routeRepository.delete(route);
        }

        private void validateRoute(RouteRequestDTO request) {

                if (request.getDepartureAirportId() == null) {

                        throw new IllegalArgumentException(
                                        "Departure airport is required");
                }

                if (request.getArrivalAirportId() == null) {

                        throw new IllegalArgumentException(
                                        "Arrival airport is required");
                }

                if (request.getDepartureAirportId()
                                .equals(request.getArrivalAirportId())) {

                        throw new IllegalArgumentException(
                                        "Departure and arrival airports cannot be the same");
                }

                if (request.getDistanceKm() == null
                                || request.getDistanceKm() <= 0) {

                        throw new IllegalArgumentException(
                                        "Distance must be greater than zero");
                }

                if (request.getEstimatedDurationMinutes() == null
                                || request.getEstimatedDurationMinutes() <= 0) {

                        throw new IllegalArgumentException(
                                        "Estimated duration must be greater than zero");
                }

                if (request.getStatus() == null
                                || request.getStatus().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Route status is required");
                }
        }
}