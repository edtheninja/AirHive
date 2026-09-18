package com.airhive.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.airhive.backend.dto.LoginRequest;
import com.airhive.backend.dto.LoginResponse;
import com.airhive.backend.dto.SignupRequest;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.exception.DuplicateResourceException;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.security.JwtService;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final AppUserRepository appUserRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        public AuthService(
                        AuthenticationManager authenticationManager,
                        AppUserRepository appUserRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {

                this.authenticationManager = authenticationManager;
                this.appUserRepository = appUserRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
        }

        public LoginResponse login(LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.username(),
                                                request.password()));

                AppUser user = appUserRepository.findByUsername(request.username())
                                .orElseThrow(() -> new IllegalStateException(
                                                "Authenticated user no longer exists"));

                String token = jwtService.generateToken(user);

                return new LoginResponse(
                                user.getId(),
                                token,
                                user.getUsername(),
                                user.getRole().name());
        }

        public LoginResponse signup(SignupRequest request) {

                if (appUserRepository.existsByUsername(request.username())) {
                        throw new DuplicateResourceException(
                                        "User already exists with username: " + request.username());
                }

                AppUser user = new AppUser();
                user.setUsername(request.username());
                user.setPassword(passwordEncoder.encode(request.password()));

                // Public signup accounts are always VIEWER accounts.
                user.setRole(Role.VIEWER);
                user.setEnabled(true);

                AppUser savedUser = appUserRepository.save(user);

                String token = jwtService.generateToken(savedUser);

                return new LoginResponse(
                                savedUser.getId(),
                                token,
                                savedUser.getUsername(),
                                savedUser.getRole().name());
        }
}