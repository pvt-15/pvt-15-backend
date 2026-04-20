package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationHours;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${jwt.expiration-hours}") long expirationHours){
        this.jwtEncoder = jwtEncoder;
        this.expirationHours = expirationHours;
    }

    public String generateToken(User user){
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pvt-15-backend")
                .issuedAt(now)
                .expiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("provider", user.getProvider().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
