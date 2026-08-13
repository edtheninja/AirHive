package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.Airport;
import com.airhive.backend.repository.AirportRepository;

@Service
public class AirportService {

    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    public Airport getAirportById(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id));
    }

    public Airport createAirport(Airport airport) {
        return airportRepository.save(airport);
    }

    public Airport updateAirport(Long id, Airport updatedAirport) {
        Airport airport = getAirportById(id);

        airport.setIataCode(updatedAirport.getIataCode());
        airport.setIcaoCode(updatedAirport.getIcaoCode());
        airport.setName(updatedAirport.getName());
        airport.setCity(updatedAirport.getCity());
        airport.setCountry(updatedAirport.getCountry());
        airport.setTerminalCount(updatedAirport.getTerminalCount());
        airport.setStatus(updatedAirport.getStatus());

        return airportRepository.save(airport);
    }

    public void deleteAirport(Long id) {
        Airport airport = getAirportById(id);
        airportRepository.delete(airport);
    }
}