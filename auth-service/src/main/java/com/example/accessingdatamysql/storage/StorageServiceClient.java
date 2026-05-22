package com.example.accessingdatamysql.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class StorageServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceClient.class);

    private final RestClient restClient;

    public StorageServiceClient(@Value("${storage.service.base-url}") String storageServiceBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(storageServiceBaseUrl)
                .build();
    }

    public String generateSignedReadUrl(String objectKey, String jwtToken) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        try {
            Map<String, String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/storage/signed-url")
                            .queryParam("objectKey", objectKey)
                            .build())
                    .headers(headers -> headers.setBearerAuth(jwtToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null) {
                return null;
            }

            return response.get("imageUrl");

        } catch (RestClientException e) {
            logger.warn("Could not generate signed profile image URL for objectKey={}", objectKey, e);
            return null;
        }
    }
}