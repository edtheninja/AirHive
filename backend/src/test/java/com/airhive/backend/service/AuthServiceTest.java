package com.airhive.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.airhive.backend.dto.LoginRequest;
import com.airhive.backend.dto.LoginResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.security.JwtService;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private AppUserRepository appUserRepository;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        appUserRepository = mock(AppUserRepository.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(
                authenticationManager,
                appUserRepository,
                jwtService
        );
    }

    @Test
    void loginReturnsTokenForValidCredentials() {

        LoginRequest request = new LoginRequest(
                "admin",
                "password"
        );

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.token());
        assertEquals("admin", response.username());
        assertEquals("ADMIN", response.role());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService).generateToken(user);
    }

    @Test
    void loginFailsWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest(
                "admin",
                "wrong-password"
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException(
                        "Invalid credentials"
                )
        );

        assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(appUserRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }
}