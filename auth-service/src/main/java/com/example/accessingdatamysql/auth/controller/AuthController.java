package com.example.accessingdatamysql.auth.controller;

import com.example.accessingdatamysql.auth.dto.AuthResponse;
import com.example.accessingdatamysql.auth.dto.GoogleLoginRequest;
import com.example.accessingdatamysql.auth.dto.LoginRequest;
import com.example.accessingdatamysql.auth.dto.RegisterRequest;
import com.example.accessingdatamysql.auth.service.AuthService;
import com.example.accessingdatamysql.auth.service.TokenRevocationService;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenRevocationService tokenRevocationService;

    public AuthController(AuthService authService,
                          UserService userService,
                          TokenRevocationService tokenRevocationService) {
        this.authService = authService;
        this.userService = userService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            AuthResponse response = authService.loginWithGoogle(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());
            UserResponse response = userService.getCurrentUser(userId, jwt.getTokenValue());
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        tokenRevocationService.revoke(jwt);
        return ResponseEntity.noContent().build();
    }
}