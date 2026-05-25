package com.example.accessingdatamysql.user.service;

import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.storage.StorageServiceClient;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String OLD_PASSWORD_REQUIRED = "Old password is required";
    private static final String NEW_PASSWORD_REQUIRED = "New password is required";
    private static final String INCORRECT_OLD_PASSWORD = "Incorrect current password";
    private static final String NOT_LOCAL_ACCOUNT = "Password can only be changed for local accounts";
    private static final String SAME_AS_OLD_PASSWORD = "New password must differ from the current password";
    private static final String PASSWORD_TOO_SHORT = "Password must be at least 10 characters";
    private static final String PASSWORD_MISSING_UPPERCASE = "Password must contain an uppercase letter";
    private static final String PASSWORD_MISSING_DIGIT = "Password must contain a digit";

    private static final int MIN_PASSWORD_LENGTH = 10;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StorageServiceClient storageServiceClient;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            StorageServiceClient storageServiceClient,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.storageServiceClient = storageServiceClient;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getCurrentUser(Integer userId, String jwtToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        String signedProfileImageUrl = storageServiceClient.generateSignedReadUrl(
                user.getProfileImageObjectKey(),
                jwtToken
        );

        return userMapper.toUserResponse(user, signedProfileImageUrl);
    }

    @Transactional
    public void deleteCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        userRepository.delete(user);
    }

    /**
     * Changes the password of an authenticated local user.
     *
     * <p>The method validates that:</p>
     * <ul>
     *     <li>both old and new password are provided</li>
     *     <li>the user exists and is a {@code LOCAL} provider account</li>
     *     <li>the supplied old password matches the stored hash</li>
     *     <li>the new password is different from the old one</li>
     *     <li>the new password meets the complexity rules
     *         (min 10 chars, at least one uppercase letter, at least one digit)</li>
     * </ul>
     *
     * @param userId      authenticated user id (from JWT subject)
     * @param oldPassword the user's current password, in plain text
     * @param newPassword the desired new password, in plain text
     * @throws IllegalArgumentException if any validation fails
     */
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException(OLD_PASSWORD_REQUIRED);
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(NEW_PASSWORD_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        if (user.getProvider() != Provider.LOCAL || user.getPasswordHash() == null) {
            throw new IllegalArgumentException(NOT_LOCAL_ACCOUNT);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(INCORRECT_OLD_PASSWORD);
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(SAME_AS_OLD_PASSWORD);
        }

        validatePasswordComplexity(newPassword);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void validatePasswordComplexity(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_SHORT);
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_MISSING_UPPERCASE);
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_MISSING_DIGIT);
        }
    }
}