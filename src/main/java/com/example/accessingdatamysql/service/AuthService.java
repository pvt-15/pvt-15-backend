package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.dto.AuthResponse;
import com.example.accessingdatamysql.dto.GoogleLoginRequest;
import com.example.accessingdatamysql.dto.LoginRequest;
import com.example.accessingdatamysql.dto.RegisterRequest;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.repository.UserRepository;
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
    private static final String NAME_REQUIRED = "Name is required";
    private static final String GOOGLE_NOT_IMPLEMENTED = "Google login is not implemented yet";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        newUser.setTotalPoints(0);
        newUser.setLevel(Level.LEVEL_1);

        User savedUser = userRepository.save(newUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                REGISTER_SUCCESS);
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
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                LOGIN_SUCCESS);
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if(request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("Google ID token is required");
        }
        throw new UnsupportedOperationException(GOOGLE_NOT_IMPLEMENTED);
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
}