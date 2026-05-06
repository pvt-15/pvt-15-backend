package com.example.accessingdatamysql.storage.service;

import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    ImageUploadResponse uploadImage(MultipartFile file, StorageFolder folder, Integer userId);

    String generateSignedReadUrl(String objectKey);
}