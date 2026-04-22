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

/**
 * Service class which generates a JWT-token for authenticated users.
 *
 * <p>Used after a successful registration or login to create a signed
 * token, which the client later send to protected endpoints.</p>
 *
 * <ul>
 *     <li>issuer: {@code pvt-15-backend}</li>
 *     <li>subject: users database-ID</li>
 *     <li>claims for email, name and provider</li>
 *     <li>an expiring time from {@code jwt.expiration-hours}</li>
 *     <li>using HS256 algorithm</li>
 * </ul>
 *
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationHours;

    /**
     * Creates a new {@code JwtService}.
     *
     * @param jwtEncoder component which signs and codes JWT-token
     * @param expirationHours total hours that the generated Token is valid
     */
    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${jwt.expiration-hours}") long expirationHours) {
        this.jwtEncoder = jwtEncoder;
        this.expirationHours = expirationHours;
    }

    /**
     * Generates a signed JWT-token for a user.
     *
     * <p>Builds a {@link JwtClaimsSet} with current time,
     * expiration time based on time alive and the user-ID as
     * subject.</p>
     *
     * <p>Beyond standard claims, the users email, name and provider
     * is included as their own claims. Then a {@link JwsHeader} is created
     * for HS256-signing and the token is encoded using the configured
     * {@link JwtEncoder}-instance.</p>
     *
     * @param user the authenticated user that the token is to represent
     * @return signed JWT-token as String
     */
    public String generateToken(User user) {
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
