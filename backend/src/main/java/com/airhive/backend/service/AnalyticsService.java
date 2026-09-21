package com.airhive.backend.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.ActivityCountDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.AircraftUtilizationDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.HistoricalActivityDTO;
import com.airhive.backend.dto.analytics.AnalyticsOverviewResponseDTO.StatusCountDTO;
import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.repository.AircraftRepository;
import com.airhive.backend.repository.FlightRepository;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;

    public AnalyticsService(
            FlightRepository flightRepository,
            AircraftRepository aircraftRepository) {

        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public AnalyticsOverviewResponseDTO getOverview() {

        List<Flight> flights = flightRepository.findAll();
        List<Aircraft> aircraft = aircraftRepository.findAll();

        AnalyticsOverviewResponseDTO response =
                new AnalyticsOverviewResponseDTO();

        response.setTotalFlights(flights.size());
        response.setDelayedFlights(
                flights.stream()
                        .filter(f -> "DELAYED".equalsIgnoreCase(f.getStatus()))
                        .count());

        response.setCancelledFlights(
                flights.stream()
                        .filter(f -> "CANCELLED".equalsIgnoreCase(f.getStatus()))
                        .count());

        response.setAirborneFlights(
                flights.stream()
                        .filter(f -> "IN AIR".equalsIgnoreCase(f.getStatus()))
                        .count());

        response.setTotalAircraft(aircraft.size());

        response.setActiveAircraft(
                aircraft.stream()
                        .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                        .count());

        response.setInactiveAircraft(
                aircraft.stream()
                        .filter(a -> "INACTIVE".equalsIgnoreCase(a.getStatus()))
                        .count());

        response.setMaintenanceAircraft(
        aircraft.stream()
                .filter(a -> "MAINTENANCE".equalsIgnoreCase(a.getStatus()))
                .count());
                
        response.setFlightStatusDistribution(
                buildStatusDistribution(flights));

        response.setAircraftUtilization(
                buildAircraftUtilization(flights, aircraft));

        response.setAirportActivity(
                buildAirportActivity(flights));

        response.setRouteActivity(
                buildRouteActivity(flights));

        response.setHistoricalActivity(
                buildHistoricalActivity(flights));

        return response;
    }

    private List<StatusCountDTO> buildStatusDistribution(
            List<Flight> flights) {

        Map<String, Long> counts = flights.stream()
                .collect(Collectors.groupingBy(
                        Flight::getStatus,
                        LinkedHashMap::new,
                        Collectors.counting()));

        return counts.entrySet()
                .stream()
                .map(entry -> new StatusCountDTO(
                        entry.getKey(),
                        entry.getValue()))
                .toList();
    }

    private List<AircraftUtilizationDTO> buildAircraftUtilization(
            List<Flight> flights,
            List<Aircraft> aircraft) {

        Map<Long, Long> flightCounts = flights.stream()
                .filter(f -> f.getAircraft() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getAircraft().getId(),
                        Collectors.counting()));

        return aircraft.stream()
                .map(aircraftItem -> new AircraftUtilizationDTO(
                        aircraftItem.getId(),
                        aircraftItem.getRegistrationNumber(),
                        aircraftItem.getAircraftType() != null
                                ? aircraftItem.getAircraftType().getTypeCode()
                                : null,
                        aircraftItem.getStatus(),
                        flightCounts.getOrDefault(
                                aircraftItem.getId(),
                                0L)))
                .sorted(Comparator.comparingLong(
                        AircraftUtilizationDTO::getAssignedFlights)
                        .reversed())
                .toList();
    }

    private List<ActivityCountDTO> buildAirportActivity(
            List<Flight> flights) {

        Map<String, long[]> activity = new LinkedHashMap<>();

        for (Flight flight : flights) {

            if (flight.getDepartureAirport() != null) {
                String code = flight.getDepartureAirport().getIataCode();

                activity.computeIfAbsent(
                        code,
                        key -> new long[2])[0]++;
            }

            if (flight.getArrivalAirport() != null) {
                String code = flight.getArrivalAirport().getIataCode();

                activity.computeIfAbsent(
                        code,
                        key -> new long[2])[1]++;
            }
        }

        return activity.entrySet()
                .stream()
                .map(entry -> new ActivityCountDTO(
                        entry.getKey(),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        entry.getValue()[0] + entry.getValue()[1]))
                .sorted(Comparator.comparingLong(
                        ActivityCountDTO::getTotal)
                        .reversed())
                .toList();
    }

    private List<ActivityCountDTO> buildRouteActivity(
            List<Flight> flights) {

        Map<String, Long> routeCounts = flights.stream()
                .filter(f -> f.getRoute() != null)
                .collect(Collectors.groupingBy(
                        f -> buildRouteName(f),
                        LinkedHashMap::new,
                        Collectors.counting()));

        return routeCounts.entrySet()
                .stream()
                .map(entry -> new ActivityCountDTO(
                        entry.getKey(),
                        0,
                        0,
                        entry.getValue()))
                .sorted(Comparator.comparingLong(
                        ActivityCountDTO::getTotal)
                        .reversed())
                .toList();
    }

    private List<HistoricalActivityDTO> buildHistoricalActivity(
            List<Flight> flights) {

        Map<LocalDate, long[]> activity = new LinkedHashMap<>();

        for (Flight flight : flights) {

            if (flight.getScheduledDeparture() == null) {
                continue;
            }

            LocalDate date =
                    flight.getScheduledDeparture().toLocalDate();

            long[] counts =
                    activity.computeIfAbsent(
                            date,
                            key -> new long[3]);

            counts[0]++;

            if ("DELAYED".equalsIgnoreCase(flight.getStatus())) {
                counts[1]++;
            }

            if ("CANCELLED".equalsIgnoreCase(flight.getStatus())) {
                counts[2]++;
            }
        }

        return activity.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new HistoricalActivityDTO(
                        entry.getKey().toString(),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        entry.getValue()[2]))
                .toList();
    }

    private String buildRouteName(Flight flight) {

        String departure = flight.getDepartureAirport() != null
                ? flight.getDepartureAirport().getIataCode()
                : "?";

        String arrival = flight.getArrivalAirport() != null
                ? flight.getArrivalAirport().getIataCode()
                : "?";

        return departure + " → " + arrival;
    }
}
