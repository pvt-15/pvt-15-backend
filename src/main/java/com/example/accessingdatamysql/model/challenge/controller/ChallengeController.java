package com.example.accessingdatamysql.model.challenge.controller;

import com.example.accessingdatamysql.model.challenge.dto.ChallengeCreateRequest;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeDetailsResponse;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeResponse;
import com.example.accessingdatamysql.model.challenge.service.ChallengeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private static final String ADMIN_KEY_NOT_CONFIGURED = "Challenge admin key is not configured";

    private final ChallengeService challengeService;
    private final String adminKey;

    public ChallengeController(ChallengeService challengeService,
                               @Value("${challenge.admin.key:}") String adminKey) {
        this.challengeService = challengeService;
        this.adminKey = adminKey;
    }

    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getActiveChallenges(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getActiveChallenges(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDetailsResponse> getChallengeById(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable Integer id) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getChallengeById(userId, id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ChallengeResponse> startChallenge(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable Integer id) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.startChallenge(userId, id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChallengeResponse>> getMyChallenges(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getMyChallenges(userId));
    }

    @PostMapping("/admin")
    public ResponseEntity<ChallengeDetailsResponse> createChallenge(@RequestHeader("ADMIN-KEY") String providedAdminKey,
                                                                    @RequestBody ChallengeCreateRequest request) {
        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalStateException(ADMIN_KEY_NOT_CONFIGURED);
        }

        if(!adminKey.equals(providedAdminKey)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(challengeService.createChallenge(request));
    }
}
