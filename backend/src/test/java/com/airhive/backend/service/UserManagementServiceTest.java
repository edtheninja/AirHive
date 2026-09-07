package com.airhive.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UpdateUserStatusRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.ResourceNotFoundException;
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
                                Role.OPERATOR);
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
                                savedUser.getPassword());
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
                                                Role.VIEWER));

                assertEquals(Role.VIEWER, response.role());
                assertEquals(true, response.enabled());
        }

        @Test
        void duplicateUsernameShouldThrowException() {

                when(appUserRepository.existsByUsername("new-user"))
                                .thenReturn(true);

                assertThrows(
                                DuplicateResourceException.class,
                                () -> userManagementService.createUser(request));

                verify(appUserRepository, never()).save(any(AppUser.class));
                verify(passwordEncoder, never()).encode(any(String.class));
        }

        @Test
        void getAllUsersShouldReturnUserResponses() {

                AppUser admin = new AppUser();
                admin.setId(1L);
                admin.setUsername("admin-user");
                admin.setPassword("hashed-admin");
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);

                AppUser viewer = new AppUser();
                viewer.setId(2L);
                viewer.setUsername("viewer-user");
                viewer.setPassword("hashed-viewer");
                viewer.setRole(Role.VIEWER);
                viewer.setEnabled(false);

                when(appUserRepository.findAll())
                                .thenReturn(List.of(admin, viewer));

                List<UserResponse> responses = userManagementService.getAllUsers();

                assertEquals(2, responses.size());

                assertEquals(1L, responses.get(0).id());
                assertEquals("admin-user", responses.get(0).username());
                assertEquals(Role.ADMIN, responses.get(0).role());
                assertTrue(responses.get(0).enabled());

                assertEquals(2L, responses.get(1).id());
                assertEquals("viewer-user", responses.get(1).username());
                assertEquals(Role.VIEWER, responses.get(1).role());
                assertFalse(responses.get(1).enabled());

                verify(appUserRepository).findAll();
        }

        @Test
        void getAllUsersShouldReturnEmptyListWhenNoUsersExist() {

                when(appUserRepository.findAll())
                                .thenReturn(List.of());

                List<UserResponse> responses = userManagementService.getAllUsers();

                assertTrue(responses.isEmpty());

                verify(appUserRepository).findAll();
        }

        @Test
        void updateUserStatusShouldDisableUser() {
                AppUser user = new AppUser();
                user.setId(1L);
                user.setUsername("operator1");
                user.setRole(Role.OPERATOR);
                user.setEnabled(true);

                UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

                when(appUserRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                when(appUserRepository.save(user))
                                .thenReturn(user);

                UserResponse response = userManagementService.updateUserStatus(1L, request);

                assertFalse(response.enabled());
                assertFalse(user.isEnabled());

                verify(appUserRepository).save(user);
        }

        @Test
        void updateUserStatusShouldEnableUser() {
                AppUser user = new AppUser();
                user.setId(1L);
                user.setUsername("operator1");
                user.setRole(Role.OPERATOR);
                user.setEnabled(false);

                UpdateUserStatusRequest request = new UpdateUserStatusRequest(true);

                when(appUserRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                when(appUserRepository.save(user))
                                .thenReturn(user);

                UserResponse response = userManagementService.updateUserStatus(1L, request);

                assertTrue(response.enabled());
                assertTrue(user.isEnabled());

                verify(appUserRepository).save(user);
        }

        @Test
        void updateUserStatusShouldThrowWhenUserDoesNotExist() {
                when(appUserRepository.findById(999L))
                                .thenReturn(Optional.empty());

                UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

                assertThrows(
                                ResourceNotFoundException.class,
                                () -> userManagementService.updateUserStatus(999L, request));

                verify(appUserRepository, never()).save(any());
        }
}
