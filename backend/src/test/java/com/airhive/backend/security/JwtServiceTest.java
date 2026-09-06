package com.airhive.backend.security;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.airhive.backend.config.JwtConfig;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {

        String secret =
                "airhive-development-secret-key-change-this-in-production-2026";

        JwtConfig jwtConfig = new JwtConfig();

        var secretKey = jwtConfig.jwtSecretKey(secret);

        jwtService = new JwtService(
                jwtConfig.jwtEncoder(secretKey),
                86_400_000L
        );

        jwtDecoder = jwtConfig.jwtDecoder(secretKey);
    }

    @Test
    void generateTokenContainsExpectedClaims() {

        AppUser user = new AppUser();
        user.setId(42L);
        user.setUsername("admin");
        user.setPassword("ignored");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("admin", jwt.getSubject());
        assertEquals(42L, jwt.<Long>getClaim("userId"));
        assertEquals("ADMIN", jwt.getClaim("role"));

        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
    }

    @Test
    void generatedTokenHasConfiguredExpiration() {

        AppUser user = new AppUser();
        user.setId(7L);
        user.setUsername("operator");
        user.setPassword("ignored");
        user.setRole(Role.OPERATOR);
        user.setEnabled(true);

        Instant before = Instant.now();

        String token = jwtService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(token);

        Instant after = Instant.now();

        assertEquals("operator", jwt.getSubject());
        assertEquals("OPERATOR", jwt.getClaim("role"));

        assertTrue(
                !jwt.getExpiresAt().isBefore(before.plusMillis(86_400_000L - 1000))
        );

        assertTrue(
                !jwt.getExpiresAt().isAfter(after.plusMillis(86_400_000L + 1000))
        );
    }
}