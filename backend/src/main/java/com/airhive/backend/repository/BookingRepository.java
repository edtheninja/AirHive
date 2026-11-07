package com.airhive.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByPnr(String pnr);

    List<Booking> findByUser_Id(Long userId);

    long countByLinkedFlight_Id(Long flightId);
}