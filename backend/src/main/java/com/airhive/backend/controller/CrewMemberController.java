package com.airhive.backend.controller;

import com.airhive.backend.dto.CrewMemberResponseDTO;
import com.airhive.backend.service.CrewMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crew")
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    public CrewMemberController(CrewMemberService crewMemberService) {
        this.crewMemberService = crewMemberService;
    }

    @GetMapping
    public ResponseEntity<List<CrewMemberResponseDTO>> getAllCrewMembers() {
        return ResponseEntity.ok(
                crewMemberService.getAllCrewMembers()
        );
    }
}
