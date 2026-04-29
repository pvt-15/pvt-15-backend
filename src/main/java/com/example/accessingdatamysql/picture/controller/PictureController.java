package com.example.accessingdatamysql.picture.controller;

import com.example.accessingdatamysql.picture.dto.*;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.picture.service.DiscoveryService;
import com.example.accessingdatamysql.picture.service.PictureService;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pictures")
public class PictureController {

    private final PictureService pictureService;
    private final DiscoveryService discoveryService;
    private final UserRepository userRepository;

    public PictureController(PictureService pictureService,
                             DiscoveryService discoveryService,
                             UserRepository userRepository) {
        this.pictureService = pictureService;
        this.discoveryService = discoveryService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PictureResponse> createPicture(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestBody CreatePictureRequest request) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        PictureResponse response = pictureService.createPicture(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PictureResponse>> getMyPictures(@AuthenticationPrincipal Jwt jwt,
                                                               @RequestParam(required = false) String category,
                                                               @RequestParam(required = false) String sort) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        List<PictureResponse> pictures = pictureService.getMyPictures(userId, category, sort);
        return ResponseEntity.ok(pictures);
    }

    @GetMapping("/stats")
    public ResponseEntity<PictureStatsResponse> getPictureStats(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        PictureStatsResponse stats = pictureService.getPictureStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/discovery-stats")
    public ResponseEntity<DiscoveryStatsResponse> getDiscoveryStats(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(discoveryService.getDiscoveryStats(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PictureResponse> getPictureById(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable Integer id) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        PictureResponse response = pictureService.getPictureById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/library")
    public ResponseEntity<List<LibraryItemResponse>> getUniqueLibrary(@AuthenticationPrincipal Jwt jwt,
                                                                      @RequestParam(required = false) String category,
                                                                      @RequestParam(required = false) String sort) {
        Integer userId = Integer.valueOf(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(discoveryService.getUniqueLibrary(user, category, sort));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePicture(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable Integer id) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        pictureService.deletePicture(userId, id);
        return ResponseEntity.noContent().build();
    }
}