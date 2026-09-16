package com.airhive.backend.mapper;

import com.airhive.backend.dto.CrewMemberResponseDTO;
import com.airhive.backend.entity.CrewMember;

public final class CrewMemberMapper {

    private CrewMemberMapper() {
    }

    public static CrewMemberResponseDTO toResponse(CrewMember crewMember) {

        CrewMemberResponseDTO dto = new CrewMemberResponseDTO();

        dto.setId(crewMember.getId());
        dto.setCrewCode(crewMember.getCrewCode());
        dto.setName(crewMember.getName());
        dto.setRole(crewMember.getRole());
        dto.setBase(crewMember.getBase());
        dto.setAvailability(crewMember.getAvailability());
        dto.setRestHours(crewMember.getRestHours());
        dto.setNextFlight(crewMember.getNextFlight());
        dto.setMedical(crewMember.getMedical());
        dto.setInitials(crewMember.getInitials());

        return dto;
    }
}
