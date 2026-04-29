package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.user.dto.UpdateProfileImageRequest;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class MainController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public MainController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            users.add(userMapper.toUserResponse(user));
        }
        return users;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);

        return user.map(userMapper::toUserResponse).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PatchMapping("/me/profile-image")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal Jwt jwt,
                                                @RequestBody UpdateProfileImageRequest request) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());

            if (request == null || request.getProfileImageUrl() == null || request.getProfileImageUrl().isBlank()) {
                return ResponseEntity.badRequest().body("profileImageUrl is required");
            }

            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = optionalUser.get();
            user.setProfileImageUrl(request.getProfileImageUrl().trim());
            userRepository.save(user);

            return ResponseEntity.ok(userMapper.toUserResponse(user));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid user id in token");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}