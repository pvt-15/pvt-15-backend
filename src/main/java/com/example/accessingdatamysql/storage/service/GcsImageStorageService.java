package com.example.accessingdatamysql.storage.service;

import com.example.accessingdatamysql.storage.config.StorageProperties;
import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsImageStorageService implements ImageStorageService {

    private final Storage storage;
    private final StorageProperties storageProperties;

    public GcsImageStorageService(Storage storage, StorageProperties storageProperties) {
        this.storage = storage;
        this.storageProperties = storageProperties;
    }

    @Override
    public ImageUploadResponse uploadImage(MultipartFile file, StorageFolder folder, Integer userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        try {
            String contentType = file.getContentType();
            String extension = getExtension(file.getOriginalFilename());
            String objectKey = buildObjectKey(folder, userId, extension);

            BlobId blobId = BlobId.of(storageProperties.getBucketName(), objectKey);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, file.getBytes());

            String imageUrl = "https://storage.googleapis.com/"
                    + storageProperties.getBucketName()
                    + "/"
                    + objectKey;

            return new ImageUploadResponse(imageUrl, objectKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not upload image to Google Cloud Storage", e);
        }
    }

    private String buildObjectKey(StorageFolder folder, Integer userId, String extension) {
        String folderName = switch (folder) {
            case PROFILE_IMAGES -> "profile-images";
            case PICTURES -> "pictures";
            case QUIZ_IMAGES -> "quiz-images";
            case CHALLENGE_IMAGES -> "challenge-images";
        };

        String ownerPart = userId == null ? "shared" : "user-" + userId;
        return folderName + "/" + ownerPart + "/" + UUID.randomUUID() + extension;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        if (extension.length() > 10) {
            return ".jpg";
        }

        return extension;
    }
}