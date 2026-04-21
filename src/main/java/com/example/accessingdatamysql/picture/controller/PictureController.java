package com.example.accessingdatamysql.picture.controller;

import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.service.PictureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pictures")
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping
    public ResponseEntity<PictureResponse> createPicture(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestBody CreatePictureRequest request) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        PictureResponse response = pictureService.createPicture(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PictureResponse>> getMyPictures(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(pictureService.getMyPictures(userId));
    }
}
