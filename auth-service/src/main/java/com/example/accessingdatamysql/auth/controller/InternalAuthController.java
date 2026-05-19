package com.example.accessingdatamysql.auth.controller;

import com.example.accessingdatamysql.auth.dto.RevocationStatusResponse;
import com.example.accessingdatamysql.auth.service.TokenRevocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final TokenRevocationService tokenRevocationService;

    public InternalAuthController(TokenRevocationService tokenRevocationService) {
        this.tokenRevocationService = tokenRevocationService;
    }

    @GetMapping("/revoked")
    public ResponseEntity<RevocationStatusResponse> isRevoked(@RequestParam String jti) {
        boolean revoked = tokenRevocationService.isRevoked(jti);
        return ResponseEntity.ok(new RevocationStatusResponse(revoked));
    }
}