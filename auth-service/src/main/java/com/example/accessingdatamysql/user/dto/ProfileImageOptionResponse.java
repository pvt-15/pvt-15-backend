package com.example.accessingdatamysql.user.dto;

public class ProfileImageOptionResponse {

    private String avatarId;
    private String objectKey;
    private String imageUrl;

    public ProfileImageOptionResponse(String avatarId, String objectKey, String imageUrl) {
        this.avatarId = avatarId;
        this.objectKey = objectKey;
        this.imageUrl = imageUrl;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}