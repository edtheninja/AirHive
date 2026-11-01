
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
import com.airhive.backend.dto.UpdateUserRoleRequest;
import com.airhive.backend.dto.UpdateUserStatusRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.GovernanceLock;
import com.airhive.backend.entity.Role;
import com.airhive.backend.entity.UserAuditLog;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.LastAdminException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.GovernanceLockRepository;
import com.airhive.backend.repository.UserAuditLogRepository;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long TARGET_ID = 2L;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GovernanceLockRepository governanceLockRepository;

    @Mock
    private UserAuditLogRepository userAuditLogRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    private CreateUserRequest request;
    private AppUser admin;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest(
                "new-user",
                "password123",
                Role.OPERATOR);

        admin = createUser(ACTOR_ID, "admin-user", Role.ADMIN, true);
    }

    private AppUser createUser(
            Long id,
            String username,
            Role role,
            boolean enabled) {

        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    private void mockGovernanceActor() {
        when(governanceLockRepository.lockById(1L))
                .thenReturn(Optional.of(GovernanceLock.of(1L)));

        when(appUserRepository.findById(ACTOR_ID))
                .thenReturn(Optional.of(admin));
    }

    @Test
    void createUserShouldSaveUserWithHashedPassword() {

        mockGovernanceActor();

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = createUser(
                3L, "new-user", Role.OPERATOR, true);
        savedUser.setPassword("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userManagementService.createUser(ACTOR_ID, request);

        assertEquals(3L, response.id());
        assertEquals("new-user", response.username());
        assertEquals(Role.OPERATOR, response.role());
        assertTrue(response.enabled());

        verify(passwordEncoder).encode("password123");
        verify(appUserRepository).saveAndFlush(any(AppUser.class));
        verify(userAuditLogRepository).save(any(UserAuditLog.class));
    }

    @Test
    void createUserShouldNotStorePlainTextPassword() {

        mockGovernanceActor();

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = createUser(
                3L, "new-user", Role.OPERATOR, true);
        savedUser.setPassword("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenReturn(savedUser);

        userManagementService.createUser(ACTOR_ID, request);

        verify(passwordEncoder).encode("password123");
        assertNotEquals("password123", savedUser.getPassword());
    }

    @Test
    void createUserShouldBeEnabledByDefault() {

        mockGovernanceActor();

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        AppUser savedUser = createUser(
                3L, "new-user", Role.VIEWER, true);
        savedUser.setPassword("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenReturn(savedUser);

        UserResponse response = userManagementService.createUser(
                ACTOR_ID,
                new CreateUserRequest(
                        "new-user",
                        "password123",
                        Role.VIEWER));

        assertEquals(Role.VIEWER, response.role());
        assertTrue(response.enabled());
    }

    @Test
    void duplicateUsernameShouldThrowException() {

        mockGovernanceActor();

        when(appUserRepository.existsByUsername("new-user"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> userManagementService.createUser(ACTOR_ID, request));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userAuditLogRepository, never())
                .save(any(UserAuditLog.class));
    }

    @Test
    void getAllUsersShouldReturnUserResponses() {

        AppUser viewer = createUser(
                2L, "viewer-user", Role.VIEWER, false);

        when(appUserRepository.findAll())
                .thenReturn(List.of(admin, viewer));

        List<UserResponse> responses =
                userManagementService.getAllUsers();

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

        List<UserResponse> responses =
                userManagementService.getAllUsers();

        assertTrue(responses.isEmpty());
        verify(appUserRepository).findAll();
    }

    @Test
    void updateUserStatusShouldDisableUser() {

        mockGovernanceActor();

        AppUser target = createUser(
                TARGET_ID, "operator1", Role.OPERATOR, true);

        when(appUserRepository.findById(TARGET_ID))
                .thenReturn(Optional.of(target));

        when(appUserRepository.saveAndFlush(target))
                .thenReturn(target);

        UserResponse response = userManagementService.updateUserStatus(
                ACTOR_ID,
                TARGET_ID,
                new UpdateUserStatusRequest(false));

        assertFalse(response.enabled());
        assertFalse(target.isEnabled());

        verify(appUserRepository).saveAndFlush(target);
        verify(userAuditLogRepository).save(any(UserAuditLog.class));
    }

    @Test
    void updateUserStatusShouldEnableUser() {

        mockGovernanceActor();

        AppUser target = createUser(
                TARGET_ID, "operator1", Role.OPERATOR, false);

        when(appUserRepository.findById(TARGET_ID))
                .thenReturn(Optional.of(target));

        when(appUserRepository.saveAndFlush(target))
                .thenReturn(target);

        UserResponse response = userManagementService.updateUserStatus(
                ACTOR_ID,
                TARGET_ID,
                new UpdateUserStatusRequest(true));

        assertTrue(response.enabled());
        assertTrue(target.isEnabled());

        verify(appUserRepository).saveAndFlush(target);
        verify(userAuditLogRepository).save(any(UserAuditLog.class));
    }

    @Test
    void updateUserStatusShouldThrowWhenUserDoesNotExist() {

        mockGovernanceActor();

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userManagementService.updateUserStatus(
                        ACTOR_ID,
                        999L,
                        new UpdateUserStatusRequest(false)));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));
    }

    @Test
    void updateUserRoleShouldChangeRoleAndWriteAudit() {

        mockGovernanceActor();

        AppUser target = createUser(
                TARGET_ID, "operator1", Role.OPERATOR, true);

        when(appUserRepository.findById(TARGET_ID))
                .thenReturn(Optional.of(target));

        when(appUserRepository.saveAndFlush(target))
                .thenReturn(target);

        UserResponse response = userManagementService.updateUserRole(
                ACTOR_ID,
                TARGET_ID,
                new UpdateUserRoleRequest(Role.VIEWER));

        assertEquals(Role.VIEWER, response.role());

        verify(appUserRepository).saveAndFlush(target);
        verify(userAuditLogRepository).save(any(UserAuditLog.class));
    }

    @Test
    void lastEnabledAdminCannotBeDemoted() {

        mockGovernanceActor();

        when(appUserRepository.countByRoleAndEnabledTrue(Role.ADMIN))
                .thenReturn(1L);

        assertThrows(
                LastAdminException.class,
                () -> userManagementService.updateUserRole(
                        ACTOR_ID,
                        ACTOR_ID,
                        new UpdateUserRoleRequest(Role.OPERATOR)));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));

        verify(userAuditLogRepository, never())
                .save(any(UserAuditLog.class));
    }

    @Test
    void lastEnabledAdminCannotBeDisabled() {

        mockGovernanceActor();

        assertThrows(
                LastAdminException.class,
                () -> userManagementService.updateUserStatus(
                        ACTOR_ID,
                        ACTOR_ID,
                        new UpdateUserStatusRequest(false)));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));

        verify(userAuditLogRepository, never())
                .save(any(UserAuditLog.class));
    }

    @Test
    void adminCanBeDemotedWhenAnotherEnabledAdminExists() {

        mockGovernanceActor();

        AppUser secondAdmin = createUser(
                TARGET_ID, "second-admin", Role.ADMIN, true);

        when(appUserRepository.findById(TARGET_ID))
                .thenReturn(Optional.of(secondAdmin));

        when(appUserRepository.countByRoleAndEnabledTrue(Role.ADMIN))
                .thenReturn(2L);

        when(appUserRepository.saveAndFlush(secondAdmin))
                .thenReturn(secondAdmin);

        UserResponse response = userManagementService.updateUserRole(
                ACTOR_ID,
                TARGET_ID,
                new UpdateUserRoleRequest(Role.OPERATOR));

        assertEquals(Role.OPERATOR, response.role());

        verify(appUserRepository).saveAndFlush(secondAdmin);
        verify(userAuditLogRepository).save(any(UserAuditLog.class));
    }

    @Test
    void disabledActorCannotPerformGovernanceOperation() {

        admin.setEnabled(false);

        when(governanceLockRepository.lockById(1L))
                .thenReturn(Optional.of(GovernanceLock.of(1L)));

        when(appUserRepository.findById(ACTOR_ID))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalStateException.class,
                () -> userManagementService.createUser(ACTOR_ID, request));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));
    }

    @Test
    void nonAdminActorCannotPerformGovernanceOperation() {

        admin.setRole(Role.OPERATOR);

        when(governanceLockRepository.lockById(1L))
                .thenReturn(Optional.of(GovernanceLock.of(1L)));

        when(appUserRepository.findById(ACTOR_ID))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalStateException.class,
                () -> userManagementService.createUser(ACTOR_ID, request));

        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));
    }

    @Test
    void missingGovernanceLockShouldPreventMutation() {

        when(governanceLockRepository.lockById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> userManagementService.createUser(ACTOR_ID, request));

        verify(appUserRepository, never()).findById(ACTOR_ID);
        verify(appUserRepository, never())
                .saveAndFlush(any(AppUser.class));
    }
}
