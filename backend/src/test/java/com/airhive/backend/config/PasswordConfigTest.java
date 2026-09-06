package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordConfigTest {

    private final PasswordEncoder passwordEncoder =
            new PasswordConfig().passwordEncoder();

    @Test
    void passwordEncoder_shouldEncodePassword() {

        String rawPassword = "airhive-password";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword));
    }
}
