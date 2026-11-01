
package com.airhive.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UpdateUserRoleRequest;
import com.airhive.backend.dto.UpdateUserStatusRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.entity.UserAuditLog;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.exception.LastAdminException;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.GovernanceLockRepository;
import com.airhive.backend.repository.UserAuditLogRepository;

@Service
public class UserManagementService {

    private static final long GOVERNANCE_LOCK_ID = 1L;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final GovernanceLockRepository governanceLockRepository;
    private final UserAuditLogRepository userAuditLogRepository;

    public UserManagementService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            GovernanceLockRepository governanceLockRepository,
            UserAuditLogRepository userAuditLogRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.governanceLockRepository = governanceLockRepository;
        this.userAuditLogRepository = userAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(Long actorUserId, CreateUserRequest request) {
        acquireGovernanceLock();
        AppUser actor = requireEnabledAdmin(actorUserId);

        if (appUserRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(
                    "User already exists with username: " + request.username());
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);

        AppUser savedUser = appUserRepository.saveAndFlush(user);

        userAuditLogRepository.save(new UserAuditLog(
                actor.getId(),
                savedUser.getId(),
                "USER_CREATED",
                null,
                "role=" + savedUser.getRole().name() + ",enabled=true"));

        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUserRole(
            Long actorUserId,
            Long userId,
            UpdateUserRoleRequest request) {

        acquireGovernanceLock();
        AppUser actor = requireEnabledAdmin(actorUserId);
        AppUser target = findUser(userId);

        Role previousRole = target.getRole();
        Role newRole = request.role();

        if (previousRole == newRole) {
            return toResponse(target);
        }

        if (target.isEnabled()
                && previousRole == Role.ADMIN
                && newRole != Role.ADMIN) {
            ensureAnotherEnabledAdminRemains();
        }

        target.setRole(newRole);
        AppUser savedUser = appUserRepository.saveAndFlush(target);

        userAuditLogRepository.save(new UserAuditLog(
                actor.getId(),
                savedUser.getId(),
                "USER_ROLE_CHANGED",
                previousRole.name(),
                newRole.name()));

        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long actorUserId,
            Long userId,
            UpdateUserStatusRequest request) {

        acquireGovernanceLock();
        AppUser actor = requireEnabledAdmin(actorUserId);
        AppUser target = findUser(userId);

        boolean previousStatus = target.isEnabled();
        boolean newStatus = request.enabled();

        if (previousStatus == newStatus) {
            return toResponse(target);
        }

        if (target.getRole() == Role.ADMIN
                && previousStatus
                && !newStatus) {
            ensureAnotherEnabledAdminRemains();
        }

        target.setEnabled(newStatus);
        AppUser savedUser = appUserRepository.saveAndFlush(target);

        userAuditLogRepository.save(new UserAuditLog(
                actor.getId(),
                savedUser.getId(),
                "USER_STATUS_CHANGED",
                previousStatus ? "enabled" : "disabled",
                newStatus ? "enabled" : "disabled"));

        return toResponse(savedUser);
    }

    private void acquireGovernanceLock() {
        governanceLockRepository.lockById(GOVERNANCE_LOCK_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Governance lock row is missing. User mutation aborted."));
    }

    private AppUser requireEnabledAdmin(Long actorUserId) {
        if (actorUserId == null) {
            throw new IllegalStateException(
                    "Authenticated administrator ID is missing.");
        }

        AppUser actor = appUserRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acting user not found with id: " + actorUserId));

        if (!actor.isEnabled() || actor.getRole() != Role.ADMIN) {
            throw new IllegalStateException(
                    "Only an enabled administrator can perform user governance.");
        }

        return actor;
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
    }

    private void ensureAnotherEnabledAdminRemains() {
        long enabledAdmins =
                appUserRepository.countByRoleAndEnabledTrue(Role.ADMIN);

        if (enabledAdmins <= 1) {
            throw new LastAdminException(
                    "The last enabled administrator cannot be demoted or disabled.");
        }
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled());
    }
}
