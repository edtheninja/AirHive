package com.airhive.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.dto.NotificationResponseDTO;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;
import com.airhive.backend.service.NotificationService;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private NotificationService notificationService;

        @TestConfiguration
        static class TestSecurityConfiguration {

                @Bean
                SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
                                throws Exception {

                        return http
                                        .csrf(csrf -> csrf.disable())
                                        .anonymous(anonymous -> anonymous
                                                        .principal("anonymousUser")
                                                        .authorities("ROLE_ANONYMOUS"))
                                        .exceptionHandling(exception -> exception
                                                        .authenticationEntryPoint(
                                                                        (request, response, authException) -> response
                                                                                        .sendError(401))
                                                        .accessDeniedHandler(
                                                                        (request, response,
                                                                                        accessDeniedException) -> response
                                                                                                        .sendError(403)))
                                        .authorizeHttpRequests(auth -> auth
                                                        .requestMatchers("/api/notifications/**")
                                                        .authenticated()
                                                        .anyRequest().permitAll())
                                        .build();
                }
        }

        private NotificationResponseDTO notification() {
                return new NotificationResponseDTO(
                                10L,
                                1L,
                                NotificationType.FLIGHT_DELAYED,
                                "Flight Delayed",
                                "Flight AH101 has been delayed.",
                                NotificationSeverity.WARNING,
                                "FLIGHT",
                                101L,
                                false,
                                LocalDateTime.of(2026, 9, 10, 12, 0),
                                null);
        }

        @Test
        void getNotificationsReturnsUserNotifications() throws Exception {

                when(notificationService.getUserNotifications(1L))
                                .thenReturn(List.of(notification()));

                mockMvc.perform(get("/api/notifications")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject("admin")
                                                .claim("userId", 1L)
                                                .claim("role", "ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(10))
                                .andExpect(jsonPath("$[0].userId").value(1))
                                .andExpect(jsonPath("$[0].type").value("FLIGHT_DELAYED"))
                                .andExpect(jsonPath("$[0].severity").value("WARNING"))
                                .andExpect(jsonPath("$[0].read").value(false));

                verify(notificationService).getUserNotifications(1L);
        }

        @Test
        void getUnreadNotificationsReturnsUnreadNotifications() throws Exception {

                when(notificationService.getUnreadNotifications(1L))
                                .thenReturn(List.of(notification()));

                mockMvc.perform(get("/api/notifications/unread")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject("admin")
                                                .claim("userId", 1L)
                                                .claim("role", "ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(10))
                                .andExpect(jsonPath("$[0].read").value(false));

                verify(notificationService).getUnreadNotifications(1L);
        }

        @Test
        void getUnreadCountReturnsCount() throws Exception {

                when(notificationService.getUnreadCount(1L))
                                .thenReturn(3L);

                mockMvc.perform(get("/api/notifications/unread/count")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject("admin")
                                                .claim("userId", 1L)
                                                .claim("role", "ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(3));

                verify(notificationService).getUnreadCount(1L);
        }

        @Test
        void markAsReadUsesAuthenticatedUserId() throws Exception {

                NotificationResponseDTO readNotification = new NotificationResponseDTO(
                                10L,
                                1L,
                                NotificationType.FLIGHT_DELAYED,
                                "Flight Delayed",
                                "Flight AH101 has been delayed.",
                                NotificationSeverity.WARNING,
                                "FLIGHT",
                                101L,
                                true,
                                LocalDateTime.of(2026, 9, 10, 12, 0),
                                LocalDateTime.of(2026, 9, 10, 13, 0));

                when(notificationService.markAsRead(1L, 10L))
                                .thenReturn(readNotification);

                mockMvc.perform(patch("/api/notifications/10/read")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject("admin")
                                                .claim("userId", 1L)
                                                .claim("role", "ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(10))
                                .andExpect(jsonPath("$.userId").value(1))
                                .andExpect(jsonPath("$.read").value(true));

                verify(notificationService).markAsRead(1L, 10L);
        }

        @Test
        void markAllAsReadUsesAuthenticatedUserId() throws Exception {

                when(notificationService.markAllAsRead(1L))
                                .thenReturn(3);

                mockMvc.perform(patch("/api/notifications/read-all")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject("admin")
                                                .claim("userId", 1L)
                                                .claim("role", "ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(3));

                verify(notificationService).markAllAsRead(1L);
        }

        @Test
        void getNotificationsRequiresAuthentication() throws Exception {

                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isUnauthorized());
        }
}
