package com.airhive.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    boolean existsByPassengerCode(String passengerCode);
}
