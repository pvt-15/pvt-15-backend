package com.example.accessingdatamysql.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class JwtConfigTest {

    @Test
    void jwtEncoderAndDecoder_shouldBeCreated_whenSecretIsLongEnough(){
        String validSecret = "12345678901234567890123456789012";
        JwtConfig jwtConfig = new JwtConfig(validSecret);

        JwtEncoder encoder = jwtConfig.jwtEncoder();
        JwtDecoder decoder = jwtConfig.jwtDecoder();

        assertNotNull(encoder);
        assertNotNull(decoder);
    }

    @Test
    void jwtEncoder_shouldThrowException_whenSecretIsTooShort() {
        JwtConfig jwtConfig = new JwtConfig("too-short-secret");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                jwtConfig::jwtEncoder
        );

        assertEquals("JWT secret must be at least 32 bytes long", exception.getMessage());
    }
}
