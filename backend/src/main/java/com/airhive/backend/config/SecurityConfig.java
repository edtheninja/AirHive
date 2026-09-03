package com.airhive.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                   "/ws",
                                "/api/airports/**",
                                "/api/aircraft-types/**",
                                "/api/aircraft/**",
                                "/api/flights/**",
                                "/api/routes/**"
                        ).permitAll()
                          .requestMatchers(
                                HttpMethod.GET,
                                "/api/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
