package com.airhive.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementService userManagementService;

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest(
                "new-user",
                "password123",
                Role.OPERATOR
        );
    }

    @Test
    void createUserShouldSaveUserWithHashedPassword() {

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setUsername("new-user");
        savedUser.setPassword("hashed-password");
        savedUser.setRole(Role.OPERATOR);
        savedUser.setEnabled(true);

        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        UserResponse response = userManagementService.createUser(request);

        assertEquals(1L, response.id());
        assertEquals("new-user", response.username());
        assertEquals(Role.OPERATOR, response.role());
        assertFalse(response.username().isBlank());

        verify(passwordEncoder).encode("password123");
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void createUserShouldNotStorePlainTextPassword() {

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setUsername("new-user");
        savedUser.setPassword("hashed-password");
        savedUser.setRole(Role.OPERATOR);
        savedUser.setEnabled(true);

        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        userManagementService.createUser(request);

        verify(passwordEncoder).encode("password123");

        assertNotEquals(
                "password123",
                savedUser.getPassword()
        );
    }

    @Test
    void createUserShouldBeEnabledByDefault() {

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setUsername("new-user");
        savedUser.setPassword("hashed-password");
        savedUser.setRole(Role.VIEWER);
        savedUser.setEnabled(true);

        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        UserResponse response = userManagementService.createUser(
                new CreateUserRequest(
                        "new-user",
                        "password123",
                        Role.VIEWER
                )
        );

        assertEquals(Role.VIEWER, response.role());
        assertEquals(true, response.enabled());
    }

    @Test
    void duplicateUsernameShouldThrowException() {

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> userManagementService.createUser(request)
        );

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }
}
