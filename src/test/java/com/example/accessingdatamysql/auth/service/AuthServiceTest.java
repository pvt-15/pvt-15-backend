package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.dto.*;
import com.example.accessingdatamysql.auth.service.AuthService;
import com.example.accessingdatamysql.auth.service.GoogleTokenVerifierService;
import com.example.accessingdatamysql.auth.service.JwtService;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    void loginWithGoogle_shouldCreateGoogleUserWhenNoMatchExists() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setToken("google-id-token");

        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                "google-provider-id-123",
                " GOOGLE@example.com ",
                "Google User"
        );

        when(googleTokenVerifierService.verify("google-id-token")).thenReturn(googleUserInfo);
        when(userRepository.findByProviderAndProviderUserId(
                Provider.GOOGLE, "google-provider-id-123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(7);
            return savedUser;
        });

        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-google-token");

        AuthResponse response = authService.loginWithGoogle(request);

        assertNotNull(response);
        assertEquals(7, response.getUserId());
        assertEquals("Google User", response.getName());
        assertEquals("google@example.com", response.getEmail());
        assertEquals("Google login successful", response.getMessage());
        assertEquals("jwt-google-token", response.getToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Google User", savedUser.getName());
        assertEquals("google@example.com", savedUser.getEmail());
        assertNull(savedUser.getPasswordHash());
        assertEquals(Provider.GOOGLE, savedUser.getProvider());
        assertEquals("google-provider-id-123", savedUser.getProviderUserId());
        assertEquals(0, savedUser.getTotalPoints());
        assertEquals(Level.LEVEL_1, savedUser.getLevel());
    }
}
