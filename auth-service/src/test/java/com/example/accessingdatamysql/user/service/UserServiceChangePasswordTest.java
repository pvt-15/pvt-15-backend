package com.example.accessingdatamysql.user.service;

import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.storage.StorageServiceClient;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserService#changePassword(Integer, String, String)}.
 *
 * <p>The tests mock the {@link UserRepository} and {@link PasswordEncoder}
 * so no database or Spring context is required. They cover each validation
 * branch plus the happy path.</p>
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceChangePasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StorageServiceClient storageServiceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User makeLocalUser(int id) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setProvider(Provider.LOCAL);
        user.setPasswordHash("hashed-old-password");
        return user;
    }

    @Test
    void changePassword_throwsWhenOldPasswordMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, null, "NewPassword1")
        );
        assertEquals("Old password is required", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void changePassword_throwsWhenNewPasswordMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "oldPassword", "  ")
        );
        assertEquals("New password is required", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void changePassword_throwsWhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(99, "oldPassword", "NewPassword1")
        );
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void changePassword_throwsWhenGoogleAccount() {
        User googleUser = new User();
        googleUser.setId(2);
        googleUser.setProvider(Provider.GOOGLE);
        googleUser.setPasswordHash(null);
        when(userRepository.findById(2)).thenReturn(Optional.of(googleUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(2, "oldPassword", "NewPassword1")
        );
        assertEquals("Password can only be changed for local accounts", ex.getMessage());
    }

    @Test
    void changePassword_throwsWhenOldPasswordIncorrect() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "hashed-old-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "wrongOld", "NewPassword1")
        );
        assertEquals("Incorrect current password", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNewSameAsOld() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword1", "hashed-old-password")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "OldPassword1", "OldPassword1")
        );
        assertEquals("New password must differ from the current password", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNewPasswordTooShort() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.matches("Short1", "hashed-old-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "oldPassword", "Short1")
        );
        assertEquals("Password must be at least 10 characters", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNewPasswordMissingUppercase() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.matches("nouppercase1", "hashed-old-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "oldPassword", "nouppercase1")
        );
        assertEquals("Password must contain an uppercase letter", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNewPasswordMissingDigit() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.matches("NoDigitsHere", "hashed-old-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1, "oldPassword", "NoDigitsHere")
        );
        assertEquals("Password must contain a digit", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_happyPath_savesUserWithNewHashedPassword() {
        User user = makeLocalUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1", "hashed-old-password")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("hashed-new-password");

        assertDoesNotThrow(
                () -> userService.changePassword(1, "oldPassword", "NewPassword1")
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-new-password", captor.getValue().getPasswordHash());
    }
}