package com.airhive.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Airport;

public interface AirportRepository extends JpaRepository<Airport, Long> {

	boolean existsByIataCode(String iataCode);

	boolean existsByIcaoCode(String icaoCode);
}
