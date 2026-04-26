package com.example.accessingdatamysql.model.challenge.controller;

import com.example.accessingdatamysql.model.challenge.dto.ChallengeDetailsResponse;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeResponse;
import com.example.accessingdatamysql.model.challenge.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService){
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getActiveChallenges(@AuthenticationPrincipal Jwt jwt){
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getActiveChallenges(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDetailsResponse> getChallengeById(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable Integer id){
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getChallengeById(userId, id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ChallengeResponse> startChallenge(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable Integer id){
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.startChallenge(userId, id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChallengeResponse>> getMyChallenges(@AuthenticationPrincipal Jwt jwt){
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(challengeService.getMyChallenges(userId));
    }
}
