package com.airhive.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.AircraftType;

public interface AircraftTypeRepository extends JpaRepository<AircraftType, Long> {
}