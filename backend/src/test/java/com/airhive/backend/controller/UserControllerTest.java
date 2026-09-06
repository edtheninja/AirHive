package com.airhive.backend.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.Role;
import com.airhive.backend.service.UserManagementService;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserManagementService userManagementService;
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateUser() throws Exception {

        when(userManagementService.createUser(any(CreateUserRequest.class)))
                .thenReturn(new UserResponse(
                        1L,
                        "new-user",
                        Role.OPERATOR,
                        true));

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "new-user",
                            "password": "password123",
                            "role": "OPERATOR"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("new-user"))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userManagementService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotCreateUser() throws Exception {

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "new-user",
                            "password": "password123",
                            "role": "VIEWER"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotCreateUser() throws Exception {

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "new-user",
                            "password": "password123",
                            "role": "VIEWER"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateUser() throws Exception {

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "new-user",
                            "password": "password123",
                            "role": "VIEWER"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidRequestShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "",
                            "password": "short",
                            "role": null
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
}
