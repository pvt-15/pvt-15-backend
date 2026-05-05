package com.example.accessingdatamysql.storage.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Configuration
public class GoogleCloudStorageConfig {

    @Bean
    public Storage storage(StorageProperties storageProperties) {
        try {
            if (storageProperties.getBucketName() == null || storageProperties.getBucketName().isBlank()) {
                throw new IllegalStateException("gcs.bucket-name is missing");
            }

            if (storageProperties.getProjectId() == null || storageProperties.getProjectId().isBlank()) {
                throw new IllegalStateException("gcs.project-id is missing");
            }

            if (storageProperties.getCredentialsBase64() == null || storageProperties.getCredentialsBase64().isBlank()) {
                throw new IllegalStateException("gcs.credentials-base64 is missing");
            }

            byte[] decodedCredentials = Base64.getDecoder().decode(storageProperties.getCredentialsBase64());

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(decodedCredentials)
            );

            return StorageOptions.newBuilder()
                    .setProjectId(storageProperties.getProjectId())
                    .setCredentials(credentials)
                    .build()
                    .getService();

        } catch (Exception e) {
            throw new IllegalStateException("Could not create Google Cloud Storage client", e);
        }
    }
}