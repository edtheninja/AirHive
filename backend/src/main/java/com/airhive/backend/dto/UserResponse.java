package com.airhive.backend.dto;

import com.airhive.backend.entity.Role;

public record UserResponse(
        Long id,
        String username,
        Role role,
        boolean enabled
) {
}
