package com.airhive.backend.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    @Test
    void viewerRoleIsConvertedToRoleViewerAuthority() {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("viewer")
                .claim("role", "VIEWER")
                .build();

        Authentication authentication = converter.convert(jwt);

        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_VIEWER"))
        );
    }

    @Test
    void operatorRoleIsConvertedToRoleOperatorAuthority() {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("operator")
                .claim("role", "OPERATOR")
                .build();

        Authentication authentication = converter.convert(jwt);

        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_OPERATOR"))
        );
    }

    @Test
    void adminRoleIsConvertedToRoleAdminAuthority() {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("admin")
                .claim("role", "ADMIN")
                .build();

        Authentication authentication = converter.convert(jwt);

        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN"))
        );
    }
}