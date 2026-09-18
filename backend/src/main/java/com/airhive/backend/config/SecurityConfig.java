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

                                                // Public endpoints
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                                                // Temporarily keep WebSocket handshake public.
                                                // JWT authentication for WebSockets will be handled
                                                // in the dedicated WebSocket security milestone.
                                                .requestMatchers("/ws/**").permitAll()
                                                // Read access
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/users")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/users/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/flights/*/status")
                                                .hasAnyRole("OPERATOR", "ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/**")
                                                .hasAnyRole("VIEWER", "OPERATOR", "ADMIN")
                                                // Create access
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/users")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/**")
                                                .hasAnyRole("OPERATOR", "ADMIN")
                                                // Update access
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/**")
                                                .hasAnyRole("OPERATOR", "ADMIN")
                                                // Delete access
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/**")
                                                .hasRole("ADMIN")
                                                // Everything else
                                                .anyRequest().authenticated());

                return http.build();
        }
}
