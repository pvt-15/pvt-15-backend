package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.storage.StorageServiceClient;
import com.example.accessingdatamysql.user.dto.UpdateProfileImageRequest;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.ProfileImagePreset;
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
    private final StorageServiceClient storageServiceClient;

    public UserController(UserRepository userRepository,
                          UserMapper userMapper,
                          UserService userService,
                          StorageServiceClient storageServiceClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService = userService;
        this.storageServiceClient = storageServiceClient;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());
            return ResponseEntity.ok(userService.getCurrentUser(userId, jwt.getTokenValue()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal Jwt jwt,
                                                @RequestBody UpdateProfileImageRequest request) {
        Integer userId = Integer.valueOf(jwt.getSubject());

        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }

        String objectKey = resolveProfileImageObjectKey(request);

        if (objectKey == null || objectKey.isBlank()) {
            return ResponseEntity.badRequest().body("Valid avatarId or profileImageObjectKey is required");
        }

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user.setProfileImageObjectKey(objectKey);
        user.setProfileImageUrl(null);
        userRepository.save(user);

        String signedProfileImageUrl = storageServiceClient.generateSignedReadUrl(
                user.getProfileImageObjectKey(),
                jwt.getTokenValue()
        );

        UserResponse response = userMapper.toUserResponse(user, signedProfileImageUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        userService.deleteCurrentUser(userId);

        return ResponseEntity.noContent().build();
    }

    private String resolveProfileImageObjectKey(UpdateProfileImageRequest request) {
        if (request.getAvatarId() != null && !request.getAvatarId().isBlank()) {
            return ProfileImagePreset.fromAvatarId(request.getAvatarId())
                    .map(ProfileImagePreset::getObjectKey)
                    .orElse(null);
        }

        if (request.getProfileImageObjectKey() != null && !request.getProfileImageObjectKey().isBlank()) {
            String objectKey = request.getProfileImageObjectKey().trim();

            if (ProfileImagePreset.isAllowedObjectKey(objectKey)) {
                return objectKey;
            }
        }

        return null;
    }
}