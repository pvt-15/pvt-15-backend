package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.entity.PasswordResetToken;
import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.auth.repository.PasswordResetTokenRepository;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PasswordResetService}.
 *
 * <p>Covers the request flow (local user, missing user, Google account) and the
 * reset flow (happy path, expired/used/unknown token, complexity rules).</p>
 */
@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    private static final String BASE_URL =
            "https://group-6-15.pvt.dsv.su.se/auth-service/auth/reset-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        // resetPageBaseUrl is normally injected via @Value, so build manually.
        service = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, mailService, BASE_URL);
    }

    private User localUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setProvider(Provider.LOCAL);
        return user;
    }

    @Test
    void requestReset_shouldStoreTokenAndSendEmail_forLocalUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(localUser()));

        service.requestReset("  TEST@example.com ");

        verify(tokenRepository).deleteByUserId(1);
        verify(tokenRepository).save(any(PasswordResetToken.class));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetEmail(eq("test@example.com"), urlCaptor.capture());
        assertTrue(urlCaptor.getValue().startsWith(BASE_URL + "?token="));
    }

    @Test
    void requestReset_shouldDoNothing_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        service.requestReset("missing@example.com");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void requestReset_shouldThrow_forGoogleAccount() {
        User google = localUser();
        google.setProvider(Provider.GOOGLE);
        google.setPasswordHash(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(google));

        assertThrows(IllegalArgumentException.class, () -> service.requestReset("test@example.com"));
        verify(mailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void requestReset_shouldThrow_whenEmailBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.requestReset("  "));
    }

    @Test
    void resetPassword_shouldThrow_forUnknownToken() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("whatever", "ValidPass99"));
    }

    @Test
    void resetPassword_shouldThrow_forExpiredToken() {
        PasswordResetToken expired = new PasswordResetToken(
                "hash", 1, Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("rawtoken", "ValidPass99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldThrow_forUsedToken() {
        PasswordResetToken used = new PasswordResetToken(
                "hash", 1, Instant.now(), Instant.now().plus(30, ChronoUnit.MINUTES));
        used.setUsed(true);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(used));

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("rawtoken", "ValidPass99"));
    }

    @Test
    void resetPassword_shouldRejectWeakPassword() {
        PasswordResetToken valid = new PasswordResetToken(
                "hash", 1, Instant.now(), Instant.now().plus(30, ChronoUnit.MINUTES));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(valid));
        when(userRepository.findById(1)).thenReturn(Optional.of(localUser()));

        // "short" -> too short, no uppercase, no digit.
        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("rawtoken", "short"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndMarkTokenUsed_onHappyPath() {
        PasswordResetToken valid = new PasswordResetToken(
                "hash", 1, Instant.now(), Instant.now().plus(30, ChronoUnit.MINUTES));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(valid));
        when(userRepository.findById(1)).thenReturn(Optional.of(localUser()));
        when(passwordEncoder.encode("ValidPass99")).thenReturn("new-hash");

        service.resetPassword("rawtoken", "ValidPass99");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("new-hash", userCaptor.getValue().getPasswordHash());

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().isUsed());
    }
}