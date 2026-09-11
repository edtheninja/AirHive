package com.airhive.backend.dto;

public record LoginResponse(
        Long userId,
        String token,
        String username,
        String role
) {}