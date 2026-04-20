package com.example.accessingdatamysql.controller;

import com.example.accessingdatamysql.dto.AuthResponse;
import com.example.accessingdatamysql.dto.GoogleLoginRequest;
import com.example.accessingdatamysql.dto.LoginRequest;
import com.example.accessingdatamysql.dto.RegisterRequest;
import com.example.accessingdatamysql.mapper.UserMapper;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.repository.UserRepository;
import com.example.accessingdatamysql.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          UserMapper userMapper){
        this.authService = authService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try{
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try{
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request){
        try{
            AuthResponse response = authService.loginWithGoogle(request);
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt){
        try{
            Integer userId = Integer.valueOf(jwt.getSubject());

            Optional<User> user = userRepository.findById(userId);

            return user.map(userMapper::toUserResponse).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }catch(NumberFormatException e){
            return ResponseEntity.badRequest().body(e);
        }
    }
}
