package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.BookingResponseDTO;
import com.airhive.backend.mapper.BookingMapper;
import com.airhive.backend.repository.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsForUser(Long userId) {
        return bookingRepository.findByUser_Id(userId)
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }
}
