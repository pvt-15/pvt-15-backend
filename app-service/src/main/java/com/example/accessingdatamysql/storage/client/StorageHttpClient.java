package com.example.accessingdatamysql.storage.client;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StorageHttpClient implements StorageClient {

    private final RestClient restClient;

    public StorageHttpClient(StorageClientProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Override
    public String generateSignedReadUrl(String objectKey) {
        SignedUrlResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/storage/signed-url")
                        .queryParam("objectKey", objectKey)
                        .build())
                .headers(headers -> addBearerToken(headers))
                .retrieve()
                .body(SignedUrlResponse.class);

        return response != null ? response.getImageUrl() : null;
    }

    @Override
    public void deleteImage(String objectKey) {
        restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/storage/object")
                        .queryParam("objectKey", objectKey)
                        .build())
                .headers(headers -> addBearerToken(headers))
                .retrieve()
                .toBodilessEntity();
    }

    private void addBearerToken(HttpHeaders headers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            headers.setBearerAuth(jwtAuthenticationToken.getToken().getTokenValue());
            return;
        }

        throw new IllegalStateException("No JWT available for internal storage call");
    }
}