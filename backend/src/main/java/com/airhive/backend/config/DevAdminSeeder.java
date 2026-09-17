package com.airhive.backend.config;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevAdminSeeder {

    @Bean
    CommandLineRunner seedDevAdmin(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (appUserRepository.existsByUsername("admin")) {
                return;
            }

            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("AirHiveAdmin123!"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            appUserRepository.save(admin);
        };
    }
}
