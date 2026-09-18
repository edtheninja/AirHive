package com.airhive.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.airhive.backend.security.JwtAuthenticationConverter;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter)))
                                .authorizeHttpRequests(auth -> auth

                                                // CORS preflight
                                                .requestMatchers(
                                                                HttpMethod.OPTIONS,
                                                                "/**")
                                                .permitAll()

                                                // Public authentication endpoints
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/login")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/signup")
                                                .permitAll()

                                                // WebSocket handshake remains public
                                                // WebSocket JWT security will be handled separately.
                                                .requestMatchers("/ws/**")
                                                .permitAll()

                                                // User administration
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/users")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/users")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/users/**")
                                                .hasRole("ADMIN")

                                                // Read access for all authenticated roles
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/**")
                                                .hasAnyRole(
                                                                "VIEWER",
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Operational creation access
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/aircraft",
                                                                "/api/aircraft-types",
                                                                "/api/airports",
                                                                "/api/flights",
                                                                "/api/routes")
                                                .hasAnyRole(
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Operational update access
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/aircraft/**",
                                                                "/api/aircraft-types/**",
                                                                "/api/airports/**",
                                                                "/api/flights/**",
                                                                "/api/routes/**")
                                                .hasAnyRole(
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Flight status changes
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/flights/*/status")
                                                .hasAnyRole(
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Notification read-state actions
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/notifications/**")
                                                .hasAnyRole(
                                                                "VIEWER",
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Notification deletion is restricted to the authenticated user's own
                                                // notifications
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/notifications")
                                                .hasAnyRole(
                                                                "VIEWER",
                                                                "OPERATOR",
                                                                "ADMIN")

                                                // Administrative deletion access
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/**")
                                                .hasRole("ADMIN")

                                                // Any unmatched endpoint requires authentication
                                                .anyRequest()
                                                .authenticated());

                return http.build();
        }
}
