
package com.airhive.backend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.airhive.backend.dto.FlightRequestDTO;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.service.FlightService;

@Configuration
@Profile("dev")
public class DevFlightSeeder {

    @Bean
    CommandLineRunner seedDevFlights(
            FlightRepository flightRepository,
            FlightService flightService
    ) {
        return args -> {
            LocalDate baseDate = LocalDate.now().plusDays(3);
            String dateSuffix = baseDate.format(
                    DateTimeFormatter.BASIC_ISO_DATE
            );

            seedFlight(
                    flightRepository,
                    flightService,
                    "AH701-" + dateSuffix,
                    27L,
                    2L,
                    2L,
                    3L,
                    baseDate.atTime(9, 0),
                    135
            );

            seedFlight(
                    flightRepository,
                    flightService,
                    "AH702-" + dateSuffix,
                    27L,
                    26L,
                    2L,
                    5L,
                    baseDate.plusDays(1).atTime(9, 0),
                    150
            );

            seedFlight(
                    flightRepository,
                    flightService,
                    "AH703-" + dateSuffix,
                    28L,
                    4L,
                    2L,
                    4L,
                    baseDate.atTime(10, 0),
                    175
            );
        };
    }

    private void seedFlight(
            FlightRepository flightRepository,
            FlightService flightService,
            String flightNumber,
            Long aircraftId,
            Long routeId,
            Long departureAirportId,
            Long arrivalAirportId,
            LocalDateTime departure,
            int durationMinutes
    ) {
        if (flightRepository.existsByFlightNumber(flightNumber)) {
            return;
        }

        FlightRequestDTO request = new FlightRequestDTO();
        request.setFlightNumber(flightNumber);
        request.setAircraftId(aircraftId);
        request.setRouteId(routeId);
        request.setDepartureAirportId(departureAirportId);
        request.setArrivalAirportId(arrivalAirportId);
        request.setScheduledDeparture(departure);
        request.setScheduledArrival(
                departure.plusMinutes(durationMinutes)
        );
        request.setStatus("SCHEDULED");

        flightService.createFlight(request);
    }
}
