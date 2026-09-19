package com.airhive.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.airhive.backend.entity.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    boolean existsByFlightNumber(String flightNumber);

    @Query("""
            SELECT COUNT(f)
            FROM Flight f
            WHERE f.aircraft.id = :aircraftId
            AND f.scheduledDeparture < :arrival
            AND f.scheduledArrival > :departure
            """)
    long countAircraftScheduleConflicts(
            @Param("aircraftId") Long aircraftId,
            @Param("arrival") LocalDateTime arrival,
            @Param("departure") LocalDateTime departure);

    @Query("""
            SELECT COUNT(f)
            FROM Flight f
            WHERE f.aircraft.id = :aircraftId
            AND f.id <> :flightId
            AND f.scheduledDeparture < :arrival
            AND f.scheduledArrival > :departure
            """)
    long countAircraftScheduleConflictsForUpdate(
            @Param("aircraftId") Long aircraftId,
            @Param("flightId") Long flightId,
            @Param("arrival") LocalDateTime arrival,
            @Param("departure") LocalDateTime departure);

    boolean existsByAircraftId(Long aircraftId);

    boolean existsByDepartureAirportId(Long airportId);

    boolean existsByArrivalAirportId(Long airportId);
    boolean existsByRouteId(Long routeId);
}