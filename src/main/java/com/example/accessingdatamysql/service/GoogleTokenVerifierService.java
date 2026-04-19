package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.dto.GoogleUserInfo;
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

@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(
            @Value("${google.client-id}") String googleClientId)
            throws GeneralSecurityException, IOException {

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleUserInfo verify(String idTokenString){
        try{
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if(idToken == null){
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String providerUserId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");

            if(email == null || email.isBlank()){
                throw new IllegalArgumentException("Google Account did not provide an email");
            }

            if(!Boolean.TRUE.equals(emailVerified)){
                throw new IllegalArgumentException("Google email is not verified");
            }

            return new GoogleUserInfo(providerUserId, email, name);
        }catch(GeneralSecurityException | IOException e){
            throw new IllegalArgumentException("Could not verify Google ID token", e);
        }
    }
}
