package com.airhive.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.service.AirportService;
import com.airhive.backend.service.BookingService;
import com.airhive.backend.service.CrewMemberService;
import com.airhive.backend.service.MaintenanceService;
import com.airhive.backend.service.PassengerService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AirportService airportService;

        @MockitoBean
        private BookingService bookingService;

        @MockitoBean
        private CrewMemberService crewMemberService;

        @MockitoBean
        private MaintenanceService maintenanceService;

        @MockitoBean
        private PassengerService passengerService;

        @Test
        void unauthenticatedGetReturns401() throws Exception {
                mockMvc.perform(get("/api/airports"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void unauthenticatedPostReturns401() throws Exception {
                mockMvc.perform(post("/api/airports")
                                .with(csrf())
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void unauthenticatedDeleteReturns401() throws Exception {
                mockMvc.perform(delete("/api/airports/1")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void viewerCanRead() throws Exception {
                mockMvc.perform(get("/api/airports")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());
        }

        @Test
        void viewerCannotCreate() throws Exception {
                mockMvc.perform(post("/api/airports")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void viewerCannotUpdate() throws Exception {
                mockMvc.perform(put("/api/airports/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void viewerCannotDelete() throws Exception {
                mockMvc.perform(delete("/api/airports/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isForbidden());
        }

        @Test
        void operatorCanCreate() throws Exception {
                mockMvc.perform(post("/api/airports")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void operatorCanUpdate() throws Exception {
                mockMvc.perform(put("/api/airports/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void operatorCannotDelete() throws Exception {
                mockMvc.perform(delete("/api/airports/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminCanDelete() throws Exception {
                mockMvc.perform(delete("/api/airports/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isNoContent());
        }

        @Test
        void viewerCannotCreateFlight() throws Exception {
                mockMvc.perform(post("/api/flights")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void operatorCanCreateFlight() throws Exception {
                mockMvc.perform(post("/api/flights")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void viewerCannotUpdateFlight() throws Exception {
                mockMvc.perform(put("/api/flights/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void operatorCanUpdateFlight() throws Exception {
                mockMvc.perform(put("/api/flights/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void viewerCannotUpdateFlightStatus() throws Exception {
                mockMvc.perform(patch("/api/flights/1/status")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER")))
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "status": "DELAYED"
                                                }
                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void operatorCanUpdateFlightStatus() throws Exception {
                mockMvc.perform(patch("/api/flights/1/status")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR")))
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "status": "DELAYED"
                                                }
                                                """))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void operatorCannotDeleteFlight() throws Exception {
                mockMvc.perform(delete("/api/flights/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminCanAccessFlightDeleteEndpoint() throws Exception {
                mockMvc.perform(delete("/api/flights/1")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isNotFound());
        }

        @Test
        void unauthenticatedReadOnlyEndpointsReturn401() throws Exception {
                mockMvc.perform(get("/api/bookings"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(get("/api/crew"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(get("/api/maintenance"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(get("/api/passengers"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void viewerCanReadReadOnlyEndpoints() throws Exception {
                mockMvc.perform(get("/api/bookings")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/crew")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/maintenance")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/passengers")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());
        }

        @Test
        void operatorCanReadReadOnlyEndpoints() throws Exception {
                mockMvc.perform(get("/api/bookings")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/crew")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/maintenance")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/passengers")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isOk());
        }

        @Test
        void adminCanReadReadOnlyEndpoints() throws Exception {
                mockMvc.perform(get("/api/bookings")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/crew")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/maintenance")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/passengers")
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isOk());
        }

        @Test
        void viewerCanDeleteOwnNotifications() throws Exception {
                mockMvc.perform(delete("/api/notifications")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isNoContent());
        }

        @Test
        void operatorCanDeleteOwnNotifications() throws Exception {
                mockMvc.perform(delete("/api/notifications")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                                .andExpect(status().isNoContent());
        }

        @Test
        void adminCanDeleteOwnNotifications() throws Exception {
                mockMvc.perform(delete("/api/notifications")
                                .with(csrf())
                                .with(jwt().authorities(
                                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                                .andExpect(status().isNoContent());
        }

        @Test
        void unauthenticatedUserCannotDeleteNotifications() throws Exception {
                mockMvc.perform(delete("/api/notifications")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }
}