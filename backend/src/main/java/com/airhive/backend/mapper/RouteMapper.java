package com.airhive.backend.mapper;

import com.airhive.backend.dto.RouteRequestDTO;
import com.airhive.backend.dto.RouteResponseDTO;
import com.airhive.backend.entity.Route;

public final class RouteMapper {

    private RouteMapper() {
    }

    public static Route toEntity(RouteRequestDTO dto) {

        Route route = new Route();

        route.setDistanceKm(dto.getDistanceKm());
        route.setEstimatedDurationMinutes(
                dto.getEstimatedDurationMinutes()
        );
        route.setStatus(dto.getStatus());

        return route;
    }

    public static RouteResponseDTO toResponse(Route route) {

        RouteResponseDTO dto = new RouteResponseDTO();

        dto.setId(route.getId());

        dto.setDepartureAirportId(
                route.getDepartureAirport().getId()
        );

        dto.setDepartureAirportCode(
                route.getDepartureAirport().getIataCode()
        );

        dto.setArrivalAirportId(
                route.getArrivalAirport().getId()
        );

        dto.setArrivalAirportCode(
                route.getArrivalAirport().getIataCode()
        );

        dto.setDistanceKm(route.getDistanceKm());

        dto.setEstimatedDurationMinutes(
                route.getEstimatedDurationMinutes()
        );

        dto.setStatus(route.getStatus());

        return dto;
    }
}