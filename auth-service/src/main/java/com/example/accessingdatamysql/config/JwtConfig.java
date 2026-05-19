package com.example.accessingdatamysql.config;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class for JWT-related safety components.
 *
 * <p>JwtConfig is responsible to create and expose Spring-beans for:</p>
 * <ul>
 *     <li>{@link JwtEncoder} to sign and create JWT-token</li>
 *     <li>{@link JwtDecoder} to verify and read JWT-token</li>
 * </ul>
 *
 * <p>Both encoder and decoder is based on the same secret key, which is
 * fetched from the applications configuration through {@code jwt.secret}</p>
 *
 * <p>To sign the token, the algorithm HS256 is used. The secret is used both
 * to sign the token when user is logged in and to verify the token with
 * protected requests.</p>
 *
 * <p>Also validates that the configured secret is a minimum of 32 bytes, since
 * that is required according to HS256-signing.</p>
 */
@Configuration
public class JwtConfig {

    private final String jwtSecret;

    /**
     * Creates a new {@code JwtConfig}.
     *
     * @param jwtSecret secret from the applications configuration
     *                  ({@code jwt.secret})
     */
    public JwtConfig(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * Creates a {@link JwtEncoder} which is created to sign and generate JWT-token
     *
     * @return configured {@link JwtEncoder}
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = getSecretKey();
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /**
     * Creates a {@link JwtDecoder} which is created to verify incoming JWT-tokens.
     *
     * @return configured {@link JwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Creates a {@link SecretKey} from the configured JWT-secret.
     *
     * <p>Secret is converted to bytes using UTF-8 and is then used to create a key
     * of type {@code HmacSHA256}. Also checks the minimum length of key</p>
     *
     * @return key for HMAC-SHA256
     * @throws IllegalArgumentException if the configured secret is shorter than
     *                                  32 bytes
     */
    private SecretKey getSecretKey() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
