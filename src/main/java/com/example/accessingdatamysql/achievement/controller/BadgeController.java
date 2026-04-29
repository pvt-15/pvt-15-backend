package com.example.accessingdatamysql.achievement.controller;

import com.example.accessingdatamysql.achievement.dto.BadgeResponse;
import com.example.accessingdatamysql.achievement.service.BadgeService;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    public BadgeController(BadgeService badgeService, UserRepository userRepository) {
        this.badgeService = badgeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBadges(@AuthenticationPrincipal Jwt jwt) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());

            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<BadgeResponse> badges = badgeService.getMyBadges(optionalUser.get());
            return ResponseEntity.ok(badges);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid user id in token");
        }
    }
}