package com.airhive.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.FlightActivity;

public interface FlightActivityRepository
        extends JpaRepository<FlightActivity, Long> {

    List<FlightActivity> findByFlightIdOrderByCreatedAtDesc(Long flightId);
}