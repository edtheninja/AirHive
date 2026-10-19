package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.CrewMemberResponseDTO;
import com.airhive.backend.entity.CrewMember;
import com.airhive.backend.repository.CrewMemberRepository;

@ExtendWith(MockitoExtension.class)
class CrewMemberServiceTest {

    @Mock
    private CrewMemberRepository crewMemberRepository;

    private CrewMemberService crewMemberService;

    @BeforeEach
    void setUp() {
        crewMemberService = new CrewMemberService(crewMemberRepository);
    }

    @Test
    void getAllCrewMembersReturnsMappedCrewMembers() {
        CrewMember crew = new CrewMember();
        crew.setCrewCode("CR-101");
        crew.setName("Alex Morgan");
        crew.setRole("Pilot");
        crew.setBase("DEL");
        crew.setAvailability("Available");
        crew.setRestHours(12);
        crew.setNextFlight("AH204");
        crew.setMedical("Valid");
        crew.setInitials("AM");

        when(crewMemberRepository.findAll()).thenReturn(List.of(crew));

        List<CrewMemberResponseDTO> result =
                crewMemberService.getAllCrewMembers();

        assertEquals(1, result.size());
        assertEquals("CR-101", result.get(0).getCrewCode());
        assertEquals("Alex Morgan", result.get(0).getName());
        assertEquals("Pilot", result.get(0).getRole());
        assertEquals("DEL", result.get(0).getBase());
        assertEquals("Available", result.get(0).getAvailability());
        assertEquals(12, result.get(0).getRestHours());
        assertEquals("AH204", result.get(0).getNextFlight());
        assertEquals("Valid", result.get(0).getMedical());
        assertEquals("AM", result.get(0).getInitials());

        verify(crewMemberRepository).findAll();
    }

    @Test
    void getAllCrewMembersReturnsEmptyListWhenNoCrewExists() {
        when(crewMemberRepository.findAll()).thenReturn(List.of());

        List<CrewMemberResponseDTO> result =
                crewMemberService.getAllCrewMembers();

        assertEquals(0, result.size());
        verify(crewMemberRepository).findAll();
    }
}
