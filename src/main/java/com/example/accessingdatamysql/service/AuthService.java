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

    private static final String NEW_USER_REGISTERED = "User registered successfully";
    private static final String EMAIL_REQUIRED = "Email is required";
    private static final String PASSWORD_REQUIRED = "Password is required";
    private static final String USER_ALREADY_EXISTS = "A user with that email already exists";
    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    private static final String NOT_LOCAL_LOGIN = "This account does not use local login";
    private static final String SUCCESSFUL_LOGIN = "Login successful";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request){
        if(request.getEmail() == null || request.getEmail().isBlank()){
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }

        if(request.getPassword() == null || request.getPassword().isBlank()){
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException(USER_ALREADY_EXISTS);
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setProvider(Provider.LOCAL);
        newUser.setTotalPoints(0);
        newUser.setLevel(Level.LEVEL_1);

        User savedUser = userRepository.save(newUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                NEW_USER_REGISTERED);
    }

    public AuthResponse login(LoginRequest request){
        if(request.getEmail() == null || request.getEmail().isBlank()){
            throw new IllegalArgumentException(EMAIL_REQUIRED);
        }

        if(request.getPassword() == null || request.getPassword().isBlank()){
            throw new IllegalArgumentException(PASSWORD_REQUIRED);
        }

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if(optionalUser.isEmpty()){
            throw new IllegalArgumentException(INVALID_EMAIL_OR_PASSWORD);
        }

        User user = optionalUser.get();

        if(user.getProvider() != Provider.LOCAL){
            throw new IllegalArgumentException(NOT_LOCAL_LOGIN);
        }

        if(user.getPasswordHash() == null || passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException(INVALID_EMAIL_OR_PASSWORD);
        }

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                SUCCESSFUL_LOGIN);
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request){
        throw new UnsupportedOperationException("Google login is not implemented yet");
    }
}
