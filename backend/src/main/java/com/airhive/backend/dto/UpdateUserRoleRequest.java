package com.airhive.backend.dto;

import jakarta.validation.constraints.NotNull;

import com.airhive.backend.entity.Role;

public record UpdateUserRoleRequest(
        @NotNull
        Role role
) {
}
