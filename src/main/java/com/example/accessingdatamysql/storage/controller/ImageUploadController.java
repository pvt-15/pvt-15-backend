package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;

    public ImageUploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/profile-image")
    public ResponseEntity<ImageUploadResponse> uploadProfileImage(@AuthenticationPrincipal Jwt jwt,
                                                                  @RequestParam("file") MultipartFile file) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(
                imageStorageService.uploadImage(file, StorageFolder.PROFILE_IMAGES, userId)
        );
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
}