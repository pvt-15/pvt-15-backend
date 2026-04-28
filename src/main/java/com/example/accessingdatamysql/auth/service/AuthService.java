package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.dto.*;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for the applications authentication
 *
 * <p>AuthService is responsible for the main scenarios:</p>
 * <ul>
 *     <li>registration of new local users</li>
 *     <li>login with email and password</li>
 *     <li>login with Google ID-token</li>
 * </ul>
 *
 * <p>If authentication is successful, {@link AuthResponse}-object is returned
 * which contains userinfo and a JWT-token.</p>
 *
 * <p>Main responsibilities</p>
 * <ul>
 *     <li>validate incoming request-objects</li>
 *     <li>normalize email for consistent database lookups</li>
 *     <li>no doublets of accounts</li>
 *     <li>differentiate between local/Google accounts</li>
 *     <li>create users with correct standard values for registration</li>
 * </ul>
 *
 * <p>Any error is signaled with {@link IllegalArgumentException}, which
 * enables the control layer to translate these to appropriate HTTP-responses.</p>
 */
@Service
public class AuthService {

    private static final String REQUEST_BODY_REQUIRED = "Request body required";
    private static final String EMAIL_REQUIRED = "Email is required";
    private static final String PASSWORD_REQUIRED = "Password is required";
    private static final String USER_ALREADY_EXISTS = "A user with that email already exists";
    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    private static final String NOT_LOCAL_LOGIN = "This account does not use local login";
    private static final String REGISTER_SUCCESS = "User registered successfully";
    private static final String LOGIN_SUCCESS = "Login successful";
    private static final String GOOGLE_LOGIN_SUCCESS = "Google login successful";
    private static final String NAME_REQUIRED = "Name is required";
    private static final String GOOGLE_TOKEN_REQUIRED = "Google ID token is required";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    /**
     * Creates a new {@code AuthService}
     * @param userRepository repository for searching and storing users
     * @param passwordEncoder component used for hashing and verification of password
     * @param jwtService service which generates JWT-token after successful authentication
     * @param googleTokenVerifierService service which verifies Google ID-token
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       GoogleTokenVerifierService googleTokenVerifierService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    /**
     * Registers a new local user
     *
     * <p>Method first validates that the request-object contains name, email
     * and password. Then normalizes the email to reduce the risk of doublets.
     * If the email is already in the system, registration is interrupted.</p>
     *
     * <p>If registration is successful, a new user is created with:</p>
     * <ul>
     *     <li>provider set to {@code LOCAL}</li>
     *     <li>hashed password</li>
     *     <li>{@code providerUserId = null}</li>
     *     <li>{code totalPoints = 0}</li>
     *     <li>start level set to {@code Level.LEVEL_1}</li>
     * </ul>
     *
     * <p>Saved user is then used to create a JWT-token which is returned to client</p>
     *
     * @param request registration data used for creating new user
     * @return authentication reply with userdata and JWT-token
     * @throws IllegalArgumentException if request is invalid, if mandatory fields are missing
     *                                  or if email is already registered
     */
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS);
        }
        User newUser = new User();
        newUser.setName(request.getName().trim());
        newUser.setEmail(normalizedEmail);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setProvider(Provider.LOCAL);
        newUser.setProviderUserId(null);
        newUser.setTotalPoints(0);
        newUser.setLevel(Level.LEVEL_1);

        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                REGISTER_SUCCESS,
                token);
    }

    /**
     * Logs in a local user with email and password.
     *
     * <p>Method first validates the request-object and then normalizes email
     * before user is fetched from database. If either the user can not be found,
     * if the account is not local or if password does not match the pre-hashed value,
     * an exception is thrown.</p>
     *
     * <p>If authentication is successful, userinfo and a JWT-token is returned.</p>
     *
     * @param request login data with email and password
     * @return authentication reply with userdata and JWT-token
     * @throws IllegalArgumentException if request is invalid, if user already exists,
     *                                  if account uses non-local login, or if the
     *                                  password is incorrect
     */
    public AuthResponse login(LoginRequest request) {
        validateLoginRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(normalizedEmail);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException(INVALID_EMAIL_OR_PASSWORD);
        }
        User user = optionalUser.get();

        if (user.getProvider() != Provider.LOCAL) {
            throw new IllegalArgumentException(NOT_LOCAL_LOGIN);
        }
        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(INVALID_EMAIL_OR_PASSWORD);
        }
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                LOGIN_SUCCESS,
                token);
    }

    /**
     * Logs in or creates a user through Google-authentication
     *
     * <p>Requires a Google ID-token. Token is verified through {@link GoogleTokenVerifierService},
     * which returns a {@link GoogleUserInfo}-object with the users Google-identity.</p>
     *
     * <p>Two main flows:</p>
     * <ol>
     *     <li>Tries to find an existing user based on {@code Provider.GOOGLE} and
     *     Googles unique user-ID.</li>
     *     <li>If no such user exists, checks if the email is already in use. If so,
     *     process is stopped to avoid multiple accounts with the same email.</li>
     * </ol>
     *
     * <p>If user is created:</p>
     * <ul>
     *     <li>name from Google-profile, or password as fallback if name is missing</li>
     *     <li>provider to {@code Google}</li>
     *     <li>providerUserId to Googles unique user-ID</li>
     *     <li>{@code totalPoints = 0}</li>
     *     <li>start level set to {@code Level.LEVEL_1}</li>
     * </ul>
     *
     * <p>If user already exists or if newly created, a JWT-token is returned to client.</p>
     *
     * @param request request-body containing Google ID-token
     * @return authentication reply with userinfo and JWT-token
     * @throws IllegalArgumentException if token is missing, if token is invalid,
     *                                  or if email is already in use
     */
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException(GOOGLE_TOKEN_REQUIRED);
        }
        GoogleUserInfo googleUserInfo = googleTokenVerifierService.verify(request.getToken());
        String normalizedEmail = normalizeEmail(googleUserInfo.getEmail());

        Optional<User> existingGoogleUser = userRepository.findByProviderAndProviderUserId(
                Provider.GOOGLE,
                googleUserInfo.getProviderUserId());
        User user;

        if (existingGoogleUser.isPresent()) {
            user = existingGoogleUser.get();
        } else {
            Optional<User> existingEmailUser = userRepository.findByEmail(normalizedEmail);

            if (existingEmailUser.isPresent()) {
                throw new IllegalArgumentException(USER_ALREADY_EXISTS);
            }
            user = new User();
            user.setName(getGoogleName(googleUserInfo));
            user.setEmail(normalizedEmail);
            user.setPasswordHash(null);
            user.setProvider(Provider.GOOGLE);
            user.setProviderUserId(googleUserInfo.getProviderUserId());
            user.setTotalPoints(0);
            user.setLevel(Level.LEVEL_1);

            user = userRepository.save(user);
        }
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                GOOGLE_LOGIN_SUCCESS,
                token);
    }

    /**
     * Validates that a registration request contains all necessary fields.
     *
     * <p>Method checks that the request is not {@code null}, and that
     * name, email and password exists and that they are not empty/whitespace.</p>
     *
     * @param request registration request which is to be validated
     * @throws IllegalArgumentException if registration request, or any mandatory fields are missing
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }
    }

    /**
     * Validates that a login request contains all necessary fields.
     *
     * <p>Method checks that the request is not {@code null}, and that
     * email and password exists and that they are not empty/whitespace.</p>
     *
     * @param request login request which is to be validated
     * @throws IllegalArgumentException if login request, or any mandatory fields are missing
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }
    }

    /**
     * Normalizes an email before comparison or storage.
     *
     * @param email email that should be normalized
     * @return normalized email
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    /**
     * Returns a username for Google-users.
     *
     * @param googleUserInfo verified userinfo from Google
     * @return name from Google, or email as fallback
     */
    private String getGoogleName(GoogleUserInfo googleUserInfo) {
        if (googleUserInfo.getName() != null && !googleUserInfo.getName().isBlank()) {
            return googleUserInfo.getName();
        }
        return googleUserInfo.getEmail();
    }
}