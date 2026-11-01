
package com.airhive.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UpdateUserRoleRequest;
import com.airhive.backend.dto.UpdateUserStatusRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.service.UserManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createUser(
                        getUserId(jwt), request));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        return ResponseEntity.ok(userManagementService.updateUserRole(
                getUserId(jwt), id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        return ResponseEntity.ok(userManagementService.updateUserStatus(
                getUserId(jwt), id, request));
    }

    private Long getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException(
                    "Authenticated JWT is required for user governance.");
        }

        return jwt.getClaim("userId");
    }
}
