package com.example.accessingdatamysql.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthRevocationClient {

    private final RestClient restClient;

    public AuthRevocationClient(@Value("${services.auth.base-url:http://localhost:8081}") String authBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authBaseUrl)
                .build();
    }

    public boolean isRevoked(String jti) {
        AuthRevocationStatusResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/auth/revoked")
                        .queryParam("jti", jti)
                        .build())
                .retrieve()
                .body(AuthRevocationStatusResponse.class);

        return response != null && response.isRevoked();
    }
}