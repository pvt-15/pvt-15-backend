package com.example.accessingdatamysql.storage.dto;

public class ImageUploadResponse {

    private String imageUrl;
    private String objectKey;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(String imageUrl, String objectKey) {
        this.imageUrl = imageUrl;
        this.objectKey = objectKey;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getObjectKey() {
        return objectKey;
    }
}