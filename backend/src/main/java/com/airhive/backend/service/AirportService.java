package com.airhive.backend.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.AirportRequestDTO;
import com.airhive.backend.dto.AirportResponseDTO;
import com.airhive.backend.entity.Airport;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.mapper.AirportMapper;
import com.airhive.backend.repository.AirportRepository;

@Service
public class AirportService {

    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @Cacheable("airports")
    public List<AirportResponseDTO> getAllAirports() {
        return airportRepository.findAll()
                .stream()
                .map(AirportMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "airports", key = "#id")
    public AirportResponseDTO getAirportById(Long id) {

        Airport airport = airportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Airport not found with id: " + id));

        return AirportMapper.toResponse(airport);
    }

    @CacheEvict(value = "airports", allEntries = true)
    public AirportResponseDTO createAirport(AirportRequestDTO request) {

        validateAirport(request);

        if (airportRepository.existsByIataCode(request.getIataCode())) {
            throw new DuplicateResourceException(
                    "Airport already exists with IATA code: "
                            + request.getIataCode());
        }

        if (airportRepository.existsByIcaoCode(request.getIcaoCode())) {
            throw new DuplicateResourceException(
                    "Airport already exists with ICAO code: "
                            + request.getIcaoCode());
        }

        Airport airport = new Airport();

        applyRequest(airport, request);

        Airport savedAirport = airportRepository.save(airport);

        return AirportMapper.toResponse(savedAirport);
    }

    @CacheEvict(value = "airports", allEntries = true)
    public AirportResponseDTO updateAirport(
            Long id,
            AirportRequestDTO request) {

        Airport airport = airportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Airport not found with id: " + id));

        validateAirport(request);

        if (!airport.getIataCode().equals(request.getIataCode())
                && airportRepository.existsByIataCode(
                        request.getIataCode())) {

            throw new DuplicateResourceException(
                    "Airport already exists with IATA code: "
                            + request.getIataCode());
        }

        if (!airport.getIcaoCode().equals(request.getIcaoCode())
                && airportRepository.existsByIcaoCode(
                        request.getIcaoCode())) {

            throw new DuplicateResourceException(
                    "Airport already exists with ICAO code: "
                            + request.getIcaoCode());
        }

        applyRequest(airport, request);

        Airport updatedAirport = airportRepository.save(airport);

        return AirportMapper.toResponse(updatedAirport);
    }

    @CacheEvict(value = "airports", allEntries = true)
    public void deleteAirport(Long id) {

        Airport airport = airportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Airport not found with id: " + id));

        airportRepository.delete(airport);
    }

    private void applyRequest(
            Airport airport,
            AirportRequestDTO request) {

        airport.setIataCode(request.getIataCode());
        airport.setIcaoCode(request.getIcaoCode());
        airport.setName(request.getName());
        airport.setCity(request.getCity());
        airport.setCountry(request.getCountry());
        airport.setTerminalCount(request.getTerminalCount());
        airport.setStatus(request.getStatus());
    }

    private void validateAirport(AirportRequestDTO request) {

        if (request.getIataCode() == null
                || request.getIataCode().isBlank()) {

            throw new IllegalArgumentException(
                    "IATA code is required");
        }

        if (request.getIcaoCode() == null
                || request.getIcaoCode().isBlank()) {

            throw new IllegalArgumentException(
                    "ICAO code is required");
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Airport name is required");
        }

        if (request.getCity() == null
                || request.getCity().isBlank()) {

            throw new IllegalArgumentException(
                    "City is required");
        }

        if (request.getCountry() == null
                || request.getCountry().isBlank()) {

            throw new IllegalArgumentException(
                    "Country is required");
        }

        if (request.getTerminalCount() == null
                || request.getTerminalCount() < 0) {

            throw new IllegalArgumentException(
                    "Terminal count cannot be negative");
        }

        if (request.getStatus() == null
                || request.getStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "Airport status is required");
        }
    }
}