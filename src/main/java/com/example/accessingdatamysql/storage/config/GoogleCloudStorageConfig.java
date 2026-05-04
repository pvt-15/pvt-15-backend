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
            if (storageProperties.getCredentialsBase64() == null || storageProperties.getCredentialsBase64().isBlank()) {
                throw new IllegalStateException("gcs.credentials.base64 is missing");
            }

            byte[] decoded = Base64.getDecoder().decode(storageProperties.getCredentialsBase64());

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(decoded)
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