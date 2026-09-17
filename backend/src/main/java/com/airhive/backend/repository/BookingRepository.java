package com.airhive.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airhive.backend.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByPnr(String pnr);
}
