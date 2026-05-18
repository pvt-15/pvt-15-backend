package com.example.accessingdatamysql.storage.client;

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
                .retrieve()
                .toBodilessEntity();
    }
}