package com.airhive.backend.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airhive.backend.dto.CreateUserRequest;
import com.airhive.backend.dto.UpdateUserStatusRequest;
import com.airhive.backend.dto.UserResponse;
import com.airhive.backend.entity.Role;
import com.airhive.backend.exception.ResourceNotFoundException;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListUsers() throws Exception {

        when(userManagementService.getAllUsers())
                .thenReturn(List.of(
                        new UserResponse(
                                1L,
                                "admin-user",
                                Role.ADMIN,
                                true),
                        new UserResponse(
                                2L,
                                "viewer-user",
                                Role.VIEWER,
                                false)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin-user"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("viewer-user"))
                .andExpect(jsonPath("$[1].role").value("VIEWER"))
                .andExpect(jsonPath("$[1].enabled").value(false));

        verify(userManagementService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotListUsers() throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotListUsers() throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotListUsers() throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
    @Test
void adminCanUpdateUserStatus() throws Exception {
    UserResponse response =
            new UserResponse(1L, "operator1", Role.OPERATOR, false);

    when(userManagementService.updateUserStatus(
            eq(1L),
            any(UpdateUserStatusRequest.class)))
            .thenReturn(response);

    mockMvc.perform(patch("/api/users/1/status")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "enabled": false
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("operator1"))
            .andExpect(jsonPath("$.role").value("OPERATOR"))
            .andExpect(jsonPath("$.enabled").value(false));
}
@Test
void operatorCannotUpdateUserStatus() throws Exception {
    mockMvc.perform(patch("/api/users/1/status")
                    .with(user("operator").roles("OPERATOR"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "enabled": false
                            }
                            """))
            .andExpect(status().isForbidden());
}
@Test
void viewerCannotUpdateUserStatus() throws Exception {
    mockMvc.perform(patch("/api/users/1/status")
                    .with(user("viewer").roles("VIEWER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "enabled": false
                            }
                            """))
            .andExpect(status().isForbidden());
}
@Test
void unauthenticatedUserCannotUpdateUserStatus() throws Exception {
    mockMvc.perform(patch("/api/users/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "enabled": false
                            }
                            """))
            .andExpect(status().isUnauthorized());
}
@Test
void invalidStatusRequestShouldReturnBadRequest() throws Exception {
    mockMvc.perform(patch("/api/users/1/status")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {}
                            """))
            .andExpect(status().isBadRequest());
}
@Test
void missingUserShouldReturnNotFound() throws Exception {
    when(userManagementService.updateUserStatus(
            eq(999L),
            any(UpdateUserStatusRequest.class)))
            .thenThrow(new ResourceNotFoundException(
                    "User not found with id: 999"
            ));

    mockMvc.perform(patch("/api/users/999/status")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "enabled": false
                            }
                            """))
            .andExpect(status().isNotFound());
}
}
