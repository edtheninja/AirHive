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

import com.airhive.backend.dto.BookingResponseDTO;
import com.airhive.backend.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getBookings(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        if (hasRole(authentication, "PASSENGER")) {
            return ResponseEntity.ok(
                    bookingService.getBookingsForUser(requireUserId(jwt)));
        }

        // VIEWER is retained for read-only staff; OPERATOR and ADMIN keep
        // operational visibility over all bookings.
        return ResponseEntity.ok(bookingService.getAllBookings());
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
