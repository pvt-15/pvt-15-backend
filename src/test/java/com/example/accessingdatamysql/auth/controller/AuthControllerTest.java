package com.example.accessingdatamysql.auth.controller;

import com.example.accessingdatamysql.auth.dto.AuthResponse;
import com.example.accessingdatamysql.auth.dto.RegisterRequest;
import com.example.accessingdatamysql.auth.service.AuthService;
import com.example.accessingdatamysql.config.SecurityConfig;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for AuthController.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void register_shouldReturnCreatedAndAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "Test.User@example.com",
                "secret123"
        );

        AuthResponse response = new AuthResponse(
                1,
                "Test User",
                "Test.User@example.com",
                "User registered successfully",
                "jwt-token"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("Test.User@example.com"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).register(any(RegisterRequest.class));
    }
}