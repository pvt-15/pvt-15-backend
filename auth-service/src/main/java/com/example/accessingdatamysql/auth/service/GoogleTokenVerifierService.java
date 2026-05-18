package com.example.accessingdatamysql.auth.service;

import com.example.accessingdatamysql.auth.dto.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Service class which verifies Google ID-token and extracts userinfo from
 * valid Google-authentication
 *
 * <p>GoogleTokenVerifierService is used to integrate Google verification library
 * before Google-based accounts are allowed to log in or to be created.</p>
 *
 * <p>Verification is configured to only accept tokens created by the applications
 * own Google client ID. After successful verification, {@link GoogleUserInfo}-object is
 * returned.</p>
 *
 * <p>Also validates that the Google-account also has an email and that the email
 * is verified by Google before login is allowed to continue.</p>
 */
@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    /**
     * Creates a verifier for Google ID-token.
     *
     * <p>Constructor builds a {@link GoogleIdTokenVerifier} with a trusted
     * HTTP-transport, Google JSON-factory and the applications configured
     * Google client ID as allowed audience.</p>
     *
     * @param googleClientId the applications Google client ID from configuration
     * @throws GeneralSecurityException if the safe transport can not be initiated
     * @throws IOException if necessary resources can not be loaded
     */
    public GoogleTokenVerifierService(@Value("${google.client-id}") String googleClientId)
            throws GeneralSecurityException, IOException {

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    /**
     * Verifies a Google ID-token and returns userinfo.
     *
     * <p>Checks if the token is cryptographically and semantically valid according
     * to Google verification. If verification fails, the process is interrupted.</p>
     *
     * <p>If verification is successful, fetches the following:</p>
     * <ul>
     *     <li>Google unique user-ID ({@code subject})</li>
     *     <li>email</li>
     *     <li>if email is verified</li>
     *     <li>name from the token payload, if it exists</li>
     * </ul>
     *
     * <p>Requires that the email exists and is verified before a
     * {@link GoogleUserInfo}-object is returned. </p>
     *
     * @param idTokenString Google ID-token that is sent from the client
     * @return verified Google userinfo
     * @throws IllegalArgumentException if the token is invalid, if email is missing,
     *                                  if email is not verified, or if token
     *                                  could not be verified technically
     */
    public GoogleUserInfo verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();

            String providerUserId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");

            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Google Account did not provide an email");
            }
            if (!Boolean.TRUE.equals(emailVerified)) {
                throw new IllegalArgumentException("Google email is not verified");
            }
            return new GoogleUserInfo(providerUserId, email, name);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Could not verify Google ID token", e);
        }
    }
}
