
package com.airhive.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

@Configuration
@Profile("dev")
public class DevPassengerUserSeeder {

    @Bean
    CommandLineRunner seedDevPassenger(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (appUserRepository.existsByUsername("passenger_e2e_02")) {
                return;
            }

            AppUser passenger = new AppUser();
            passenger.setUsername("passenger_e2e_02");
            passenger.setPassword(
                    passwordEncoder.encode("PassengerTest456!")
            );
            passenger.setRole(Role.PASSENGER);
            passenger.setEnabled(true);

            appUserRepository.save(passenger);
        };
    }
}
