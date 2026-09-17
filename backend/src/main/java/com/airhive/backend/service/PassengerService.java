package com.airhive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.PassengerResponseDTO;
import com.airhive.backend.mapper.PassengerMapper;
import com.airhive.backend.repository.PassengerRepository;

@Service
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public List<PassengerResponseDTO> getAllPassengers() {
        return passengerRepository.findAll()
                .stream()
                .map(PassengerMapper::toResponse)
                .toList();
    }
}
