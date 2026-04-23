package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.auth.dto.AuthResponse;
import com.example.accessingdatamysql.auth.dto.LoginRequest;
import com.example.accessingdatamysql.auth.dto.RegisterRequest;
import com.example.accessingdatamysql.auth.service.AuthService;
import com.example.accessingdatamysql.auth.service.GoogleTokenVerifierService;
import com.example.accessingdatamysql.auth.service.JwtService;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateLocalUserAndReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("TestName");
        request.setEmail("   TEST.NAME@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("test.name@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1);
            return savedUser;
        });
        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1, response.getUserId());
        assertEquals("TestName", response.getName());
        assertEquals("test.name@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setName("TestName");
        request.setEmail("test.name@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("test.name@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_shouldReturnTokenForValidLocalUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("  USER@example.com ");
        request.setPassword("secret123");

        User user = new User();
        user.setId(5);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        user.setProvider(Provider.LOCAL);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-login-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(5, response.getUserId());
        assertEquals("Test User", response.getName());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-login-token", response.getToken());
    }
}
