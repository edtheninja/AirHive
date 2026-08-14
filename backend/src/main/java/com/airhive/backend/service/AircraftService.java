package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.entity.Aircraft;
import com.airhive.backend.repository.AircraftRepository;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public List<Aircraft> getAllAircraft() {
        return aircraftRepository.findAll();
    }

    public Aircraft getAircraftById(Long id) {
        return aircraftRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Aircraft not found with id: " + id));
    }

    public Aircraft createAircraft(Aircraft aircraft) {
        if (aircraftRepository.existsByRegistrationNumber(
                aircraft.getRegistrationNumber())) {

            throw new RuntimeException(
                    "Aircraft already exists with registration number: "
                            + aircraft.getRegistrationNumber());
        }

        return aircraftRepository.save(aircraft);
    }

    public Aircraft updateAircraft(Long id, Aircraft updatedAircraft) {

        Aircraft aircraft = getAircraftById(id);

        aircraft.setRegistrationNumber(
                updatedAircraft.getRegistrationNumber()
        );

        aircraft.setStatus(
                updatedAircraft.getStatus()
        );

        aircraft.setAircraftType(
                updatedAircraft.getAircraftType()
        );

        return aircraftRepository.save(aircraft);
    }

    public void deleteAircraft(Long id) {
        Aircraft aircraft = getAircraftById(id);
        aircraftRepository.delete(aircraft);
    }
}