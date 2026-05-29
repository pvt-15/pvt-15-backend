package com.example.accessingdatamysql.user.service;

import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.storage.StorageServiceClient;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.ProfileImagePreset;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final JdbcTemplate jdbcTemplate;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            StorageServiceClient storageServiceClient,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.storageServiceClient = storageServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
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
    public void deleteCurrentUser(Integer userId, String jwtToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        List<String> imageObjectKeys = findUserImageObjectKeys(user);

        deleteUserRelatedRows(userId);

        for (String objectKey : imageObjectKeys) {
            storageServiceClient.deleteObject(objectKey, jwtToken);
        }
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

    private List<String> findUserImageObjectKeys(User user) {
        List<String> imageObjectKeys = new ArrayList<>();

        List<String> pictureObjectKeys = jdbcTemplate.queryForList(
                "SELECT image_object_key FROM picture " +
                        "WHERE `user` = ? " +
                        "AND image_object_key IS NOT NULL " +
                        "AND image_object_key <> ''",
                String.class,
                user.getId()
        );

        imageObjectKeys.addAll(pictureObjectKeys);

        String profileImageObjectKey = user.getProfileImageObjectKey();

        if (profileImageObjectKey != null
                && !profileImageObjectKey.isBlank()
                && !ProfileImagePreset.isAllowedObjectKey(profileImageObjectKey)) {
            imageObjectKeys.add(profileImageObjectKey);
        }

        return imageObjectKeys;
    }

    private void deleteUserRelatedRows(Integer userId) {
        /*
         * Delete challenge-picture matches first.
         * They can point both to user's challenge task progress and to user's pictures.
         */
        jdbcTemplate.update(
                "DELETE FROM user_challenge_picture_match " +
                        "WHERE picture_id IN (" +
                        "SELECT id FROM picture WHERE `user` = ?" +
                        ")",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_challenge_picture_match " +
                        "WHERE user_challenge_task_progress_id IN (" +
                        "SELECT uctp.id " +
                        "FROM user_challenge_task_progress uctp " +
                        "JOIN user_challenge_progress ucp " +
                        "ON uctp.user_challenge_progress_id = ucp.id " +
                        "WHERE ucp.user_id = ?" +
                        ")",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_challenge_task_progress " +
                        "WHERE user_challenge_progress_id IN (" +
                        "SELECT id FROM user_challenge_progress WHERE user_id = ?" +
                        ")",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_challenge_progress WHERE user_id = ?",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_quiz_answer " +
                        "WHERE user_quiz_attempt_id IN (" +
                        "SELECT id FROM user_quiz_attempt WHERE user_id = ?" +
                        ")",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_quiz_attempt WHERE user_id = ?",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_badge WHERE user_id = ?",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM user_discovery WHERE user_id = ?",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM picture WHERE `user` = ?",
                userId
        );

        jdbcTemplate.update(
                "DELETE FROM revoked_tokens WHERE user_id = ?",
                userId
        );

        deleteOptionalPasswordResetTokens(userId);

        jdbcTemplate.update(
                "DELETE FROM `user` WHERE id = ?",
                userId
        );
    }

    private void deleteOptionalPasswordResetTokens(Integer userId) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM password_reset_token WHERE user_id = ?",
                    userId
            );
        } catch (DataAccessException e) {
            logger.debug("Could not delete password reset tokens for userId={}. " +
                    "This is ignored because the table or column may not exist in all environments.", userId);
        }
    }
}