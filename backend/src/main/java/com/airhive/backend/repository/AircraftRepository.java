package com.airhive.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Aircraft;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByAircraftTypeId(Long aircraftTypeId);
    boolean existsByRegistrationNumberAndIdNot(
        String registrationNumber,
        Long id);
}