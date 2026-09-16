package com.airhive.backend.service;

import com.airhive.backend.dto.CrewMemberResponseDTO;
import com.airhive.backend.mapper.CrewMemberMapper;
import com.airhive.backend.repository.CrewMemberRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrewMemberService {

    private final CrewMemberRepository crewMemberRepository;

    public CrewMemberService(CrewMemberRepository crewMemberRepository) {
        this.crewMemberRepository = crewMemberRepository;
    }

    @Cacheable("crew")
    public List<CrewMemberResponseDTO> getAllCrewMembers() {
        return crewMemberRepository.findAll()
                .stream()
                .map(CrewMemberMapper::toResponse)
                .toList();
    }
}
