package com.airhive.backend.security;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private AppUserDetailsService appUserDetailsService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        appUserDetailsService =
                new AppUserDetailsService(appUserRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {

        AppUser appUser = new AppUser();
        appUser.setUsername("admin");
        appUser.setPassword("encoded-password");
        appUser.setRole(Role.ADMIN);
        appUser.setEnabled(true);

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(appUser));

        UserDetails result =
                appUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));

        verify(appUserRepository).findByUsername("admin");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsDisabled() {

        AppUser appUser = new AppUser();
        appUser.setUsername("viewer");
        appUser.setPassword("encoded-password");
        appUser.setRole(Role.VIEWER);
        appUser.setEnabled(false);

        when(appUserRepository.findByUsername("viewer"))
                .thenReturn(Optional.of(appUser));

        UserDetails result =
                appUserDetailsService.loadUserByUsername("viewer");

        assertFalse(result.isEnabled());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        when(appUserRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService
                        .loadUserByUsername("missing"));

        assertEquals(
                "User not found: missing",
                exception.getMessage());

        verify(appUserRepository).findByUsername("missing");
    }
}
