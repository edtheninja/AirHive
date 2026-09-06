package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;

@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUsername_shouldReturnUser_whenUsernameExists() {

        AppUser user = saveUser("admin");

        var result = appUserRepository.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals("admin", result.get().getUsername());
        assertEquals(Role.ADMIN, result.get().getRole());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {

        var result = appUserRepository.findByUsername("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {

        saveUser("operator");

        assertTrue(
                appUserRepository.existsByUsername("operator"));
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUsernameDoesNotExist() {

        assertFalse(
                appUserRepository.existsByUsername("missing"));
    }

    private AppUser saveUser(String username) {

        AppUser user = new AppUser();

        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        return appUserRepository.saveAndFlush(user);
    }
}
