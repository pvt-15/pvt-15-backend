package com.example.accessingdatamysql.storage.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleCloudStorageConfig {

    @Bean
    public Storage storage(StorageProperties storageProperties) {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(
                            storageProperties.getCredentialsJson().getBytes(StandardCharsets.UTF_8)
                    )
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