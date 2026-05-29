package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.entity.PasswordResetToken;
import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.auth.repository.PasswordResetTokenRepository;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handles the "forgot password" flow for local accounts.
 *
 * <p>Two responsibilities:</p>
 * <ol>
 *     <li>{@link #requestReset(String)} – issue a single-use token and email a
 *     reset link to the user.</li>
 *     <li>{@link #resetPassword(String, String)} – validate a token and set the
 *     new password.</li>
 * </ol>
 *
 * <p>Security choices:</p>
 * <ul>
 *     <li>Only a SHA-256 hash of the token is stored, so a database leak does
 *     not expose working reset links.</li>
 *     <li>A non-existent email does not raise an error, to avoid letting the
 *     endpoint be used to discover which emails are registered.</li>
 *     <li>Google accounts are refused with a clear message, since they have no
 *     local password to reset.</li>
 *     <li>Requesting a new link invalidates any previous outstanding link for
 *     that user.</li>
 * </ul>
 *
 * <p>Validation failures are signalled with {@link IllegalArgumentException} so
 * the controller can translate them to HTTP responses, matching the pattern
 * used elsewhere in this service.</p>
 */
@Service
public class PasswordResetService {

    private static final String EMAIL_REQUIRED = "Email is required";
    private static final String TOKEN_REQUIRED = "Token is required";
    private static final String NEW_PASSWORD_REQUIRED = "New password is required";
    private static final String GOOGLE_ACCOUNT =
            "This account uses Google sign-in and has no password to reset";
    private static final String INVALID_OR_EXPIRED_TOKEN =
            "The reset link is invalid or has expired";
    private static final String PASSWORD_TOO_SHORT = "Password must be at least 10 characters";
    private static final String PASSWORD_MISSING_UPPERCASE = "Password must contain an uppercase letter";
    private static final String PASSWORD_MISSING_DIGIT = "Password must contain a digit";

    private static final int MIN_PASSWORD_LENGTH = 10;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    private static final long TOKEN_TTL_MINUTES = 30;
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Base URL of the reset page, e.g.
     * {@code https://group-6-15.pvt.dsv.su.se/auth-service/auth/reset-password}.
     * The token is appended as a query parameter.
     */
    private final String resetPageBaseUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                MailService mailService,
                                @Value("${app.password-reset.base-url}") String resetPageBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.resetPageBaseUrl = resetPageBaseUrl;
    }

    /**
     * Starts a password reset for the given email.
     *
     * <p>If a local user exists, a new single-use token is created and a reset
     * link is emailed. If no user exists, the method returns silently (no
     * enumeration). If the account is a Google account, an exception is thrown
     * so the caller can tell the user to sign in with Google instead.</p>
     *
     * @param rawEmail the email address from the request
     * @throws IllegalArgumentException if the email is blank, or if the account
     *                                  is a Google account
     */
    @Transactional
    public void requestReset(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }
        String email = normalizeEmail(rawEmail);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            // Do not reveal whether the email is registered.
            return;
        }
        User user = optionalUser.get();

        if (user.getProvider() != Provider.LOCAL || user.getPasswordHash() == null) {
            throw new IllegalArgumentException(GOOGLE_ACCOUNT);
        }

        // Invalidate any previous outstanding tokens for this user.
        tokenRepository.deleteByUserId(user.getId());

        String rawToken = generateRawToken();
        Instant now = Instant.now();
        PasswordResetToken token = new PasswordResetToken(
                hash(rawToken),
                user.getId(),
                now,
                now.plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES));
        tokenRepository.save(token);

        String resetUrl = resetPageBaseUrl + "?token=" + rawToken;
        mailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    /**
     * Completes a password reset.
     *
     * @param rawToken    the raw token from the emailed link
     * @param newPassword the new password to set
     * @throws IllegalArgumentException if the token is missing/invalid/expired/used,
     *                                  if the new password is blank, if the account is
     *                                  not a local account, or if the password fails
     *                                  the complexity rules
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(TOKEN_REQUIRED);
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(NEW_PASSWORD_REQUIRED);
        }

        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_OR_EXPIRED_TOKEN));

        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException(INVALID_OR_EXPIRED_TOKEN);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(INVALID_OR_EXPIRED_TOKEN));

        if (user.getProvider() != Provider.LOCAL || user.getPasswordHash() == null) {
            throw new IllegalArgumentException(GOOGLE_ACCOUNT);
        }

        validatePasswordComplexity(newPassword);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
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

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the hex-encoded SHA-256 hash of the given value.
     */
    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}