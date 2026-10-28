package com.airhive.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airhive.backend.dto.PassengerResponseDTO;
import com.airhive.backend.service.PassengerService;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping
    public ResponseEntity<List<PassengerResponseDTO>> getPassengers(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        if (hasRole(authentication, "PASSENGER")) {
            return ResponseEntity.ok(
                    passengerService.getPassengersForUser(requireUserId(jwt)));
        }

        // VIEWER is read-only staff; OPERATOR and ADMIN retain full operational
        // visibility. Passenger accounts are always scoped to their own user ID.
        return ResponseEntity.ok(passengerService.getAllPassengers());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role));
    }

    private Long requireUserId(Jwt jwt) {
        Object claim = jwt.getClaims().get("userId");
        if (claim instanceof Number number) {
            return number.longValue();
        }
        throw new AccessDeniedException("Passenger token has no valid user identity.");
    }
}
