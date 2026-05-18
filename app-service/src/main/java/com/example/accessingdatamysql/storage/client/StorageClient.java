package com.example.accessingdatamysql.storage.client;

public interface StorageClient {
    String generateSignedReadUrl(String objectKey);
    void deleteImage(String objectKey);
}