package com.airhive.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.repository.AppUserRepository;

@Service
public class UserManagementService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request) {

        if (appUserRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(
                    "User already exists with username: " + request.username()
            );
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);

        AppUser savedUser = appUserRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole(),
                savedUser.isEnabled()
        );
    }
}
