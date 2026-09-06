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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.service.AirportService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirportService airportService;

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
}