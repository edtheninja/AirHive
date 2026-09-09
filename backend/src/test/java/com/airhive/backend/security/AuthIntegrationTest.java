package com.airhive.backend.security;

import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.service.AirportService;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AirportService airportService;

    @BeforeEach
    void setUp() {

        appUserRepository.deleteAll();

        AppUser user = new AppUser();
        user.setUsername("integration-viewer");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.VIEWER);
        user.setEnabled(true);

        appUserRepository.saveAndFlush(user);
    }

    @Test
    void loginShouldReturnJwtForValidCredentials() throws Exception {

        String response = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-viewer",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("\"token\""));
        assertTrue(response.contains("\"username\":\"integration-viewer\""));
        assertTrue(response.contains("\"role\":\"VIEWER\""));
    }

    @Test
    void loginShouldRejectInvalidPassword() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-viewer",
                            "password": "wrong-password"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldRejectUnknownUser() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "missing-user",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginTokenShouldAuthorizeProtectedEndpoint() throws Exception {

        String response = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-viewer",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new JSONObject(response).getString("token");

        assertNotNull(token);
        assertFalse(token.isBlank());

        mockMvc.perform(get("/api/airports")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void disabledUserShouldNotBeAbleToLogin() throws Exception {
        AppUser disabledUser = new AppUser();
        disabledUser.setUsername("integration-disabled");
        disabledUser.setPassword(passwordEncoder.encode("password123"));
        disabledUser.setRole(Role.VIEWER);
        disabledUser.setEnabled(false);

        appUserRepository.save(disabledUser);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-disabled",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void previouslyIssuedTokenRemainsValidAfterUserIsDisabled() throws Exception {

        String response = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-viewer",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new JSONObject(response).getString("token");

        AppUser user = appUserRepository.findByUsername("integration-viewer")
                .orElseThrow();

        user.setEnabled(false);
        appUserRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/airports")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithBlankUsernameShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithBlankPasswordShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "integration-viewer",
                            "password": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidJwtShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/airports")
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingJwtShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isUnauthorized());
    }
}