package com.airhive.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByDepartureAirportIdAndArrivalAirportId(
            Long departureAirportId,
            Long arrivalAirportId
    );

    boolean existsByDepartureAirportIdAndArrivalAirportId(
            Long departureAirportId,
            Long arrivalAirportId
    );
}