package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.user.dto.UpdateProfileImageRequest;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import com.example.accessingdatamysql.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserRepository userRepository,
                          UserMapper userMapper,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal Jwt jwt,
                                                @RequestBody UpdateProfileImageRequest request) {
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

        UserResponse response = userMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        userService.deleteCurrentUser(userId);

        return ResponseEntity.noContent().build();
    }
}