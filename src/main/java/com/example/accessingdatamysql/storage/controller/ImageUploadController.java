package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.config.StorageProperties;
import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/uploads")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;
    private final StorageProperties storageProperties;

    public ImageUploadController(ImageStorageService imageStorageService,
                                 StorageProperties storageProperties) {
        this.imageStorageService = imageStorageService;
        this.storageProperties = storageProperties;
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam("file") MultipartFile file) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());
            return ResponseEntity.ok(
                    imageStorageService.uploadImage(file, StorageFolder.PROFILE_IMAGES, userId)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/picture")
    public ResponseEntity<ImageUploadResponse> uploadPicture(@AuthenticationPrincipal Jwt jwt,
                                                             @RequestParam("file") MultipartFile file) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(
                imageStorageService.uploadImage(file, StorageFolder.PICTURES, userId)
        );
    }

    @PostMapping("/challenge-image")
    public ResponseEntity<ImageUploadResponse> uploadChallengeImage(@AuthenticationPrincipal Jwt jwt,
                                                                    @RequestParam("file") MultipartFile file) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(
                imageStorageService.uploadImage(file, StorageFolder.CHALLENGE_IMAGES, userId)
        );
    }

    @GetMapping("/debug/storage")
    public ResponseEntity<Map<String, Object>> debugStorage() {
        return ResponseEntity.ok(Map.of(
                "bucketNamePresent", storageProperties.getBucketName() != null && !storageProperties.getBucketName().isBlank(),
                "projectIdPresent", storageProperties.getProjectId() != null && !storageProperties.getProjectId().isBlank(),
                "credentialsBase64Present", storageProperties.getCredentialsBase64() != null && !storageProperties.getCredentialsBase64().isBlank(),
                "bucketNameLength", storageProperties.getBucketName() == null ? 0 : storageProperties.getBucketName().length(),
                "projectIdLength", storageProperties.getProjectId() == null ? 0 : storageProperties.getProjectId().length(),
                "credentialsBase64Length", storageProperties.getCredentialsBase64() == null ? 0 : storageProperties.getCredentialsBase64().length()
        ));
    }
}