package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.dto.*;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       GoogleTokenVerifierService googleTokenVerifierService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());

        if(userRepository.existsByEmail(normalizedEmail)) {
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

    public AuthResponse login(LoginRequest request) {
        validateLoginRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(normalizedEmail);

        if(optionalUser.isEmpty()) {
            throw new IllegalArgumentException(INVALID_EMAIL_OR_PASSWORD);
        }
        User user = optionalUser.get();

        if(user.getProvider() != Provider.LOCAL) {
            throw new IllegalArgumentException(NOT_LOCAL_LOGIN);
        }
        if(user.getPasswordHash() == null ||
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

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if(request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException(GOOGLE_TOKEN_REQUIRED);
        }
        GoogleUserInfo googleUserInfo = googleTokenVerifierService.verify(request.getToken());
        String normalizedEmail = normalizeEmail(googleUserInfo.getEmail());

        Optional<User> existingGoogleUser = userRepository.findByProviderAndProviderUserId(
                Provider.GOOGLE,
                googleUserInfo.getProviderUserId());
        User user;

        if(existingGoogleUser.isPresent()){
            user = existingGoogleUser.get();
        }else{
            Optional<User> existingEmailUser = userRepository.findByEmail(normalizedEmail);

            if(existingEmailUser.isPresent()){
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

    private void validateRegisterRequest(RegisterRequest request) {
        if(request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        if(request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        if(request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }
        if(request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if(request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        if(request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }
        if(request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String getGoogleName(GoogleUserInfo googleUserInfo){
        if(googleUserInfo.getName() != null && !googleUserInfo.getName().isBlank()){
            return googleUserInfo.getName();
        }
        return googleUserInfo.getEmail();
    }
}