package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/storage")
public class InternalStorageController {

    private final ImageStorageService imageStorageService;

    public InternalStorageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/signed-url")
    public ResponseEntity<Map<String, String>> generateSignedUrl(@RequestParam String objectKey) {
        String imageUrl = imageStorageService.generateSignedReadUrl(objectKey);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/object")
    public ResponseEntity<Void> deleteObject(@RequestParam String objectKey) {
        imageStorageService.deleteImage(objectKey);
        return ResponseEntity.noContent().build();
    }
}