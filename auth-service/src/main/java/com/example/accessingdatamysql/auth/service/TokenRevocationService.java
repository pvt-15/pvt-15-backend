package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.entity.RevokedToken;
import com.example.accessingdatamysql.auth.repository.RevokedTokenRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenRevocationService {

    private static final String MISSING_JTI = "JWT is missing jti claim";
    private static final String MISSING_EXP = "JWT is missing exp claim";

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public void revoke(Jwt jwt) {
        String jti = jwt.getClaimAsString("jti");
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException(MISSING_JTI);
        }

        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            throw new IllegalArgumentException(MISSING_EXP);
        }

        if (revokedTokenRepository.existsByJti(jti)) {
            return;
        }

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jti);
        revokedToken.setUserId(Integer.valueOf(jwt.getSubject()));
        revokedToken.setRevokedAt(Instant.now());
        revokedToken.setExpiresAt(expiresAt);

        revokedTokenRepository.save(revokedToken);
    }

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return true;
        }
        return revokedTokenRepository.existsByJti(jti);
    }

    public void deleteExpiredRevocations() {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}