package com.airhive.backend.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import com.airhive.backend.config.JwtConfig;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

import static org.mockito.Mockito.mock;

class JwtRoleFreshnessTest {

    private static final String USERNAME = "jwt-role-test";
    private static final Long USER_ID = 42L;

    private AppUserRepository appUserRepository;
    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        SecretKey key = new SecretKeySpec(
                "airhive-test-secret-key-at-least-32-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        appUserRepository = mock(AppUserRepository.class);
        JwtConfig config = new JwtConfig();
        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key, appUserRepository);

        currentUser = new AppUser();
        currentUser.setId(USER_ID);
        currentUser.setUsername(USERNAME);
        currentUser.setPassword("test-password-hash");
        currentUser.setRole(Role.PASSENGER);
        currentUser.setEnabled(true);

        when(appUserRepository.findByUsername(USERNAME))
                .thenAnswer(invocation -> Optional.of(currentUser));
    }

    @Test
    void rejectsPreviouslyIssuedTokenAfterRoleChanges() {
        String staleToken = tokenWithRole("VIEWER", USER_ID);

        assertThrows(JwtException.class, () -> decoder.decode(staleToken));
    }

    @Test
    void acceptsTokenWhenRoleAndUserIdMatchCurrentAccount() {
        String currentToken = tokenWithRole("PASSENGER", USER_ID);

        assertDoesNotThrow(() -> decoder.decode(currentToken));
    }

    @Test
    void rejectsTokenWhenUserIdDoesNotMatchCurrentAccount() {
        String mismatchedToken = tokenWithRole("PASSENGER", 99L);

        assertThrows(JwtException.class, () -> decoder.decode(mismatchedToken));
    }

    private String tokenWithRole(String role, Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(USERNAME)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(() -> "HS256").build(),
                claims)).getTokenValue();
    }
}
