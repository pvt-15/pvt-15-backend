package com.example.accessingdatamysql.user.dto;

public class UpdateProfileImageRequest {

    private String avatarId;
    private String profileImageObjectKey;

    public UpdateProfileImageRequest() {
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getProfileImageObjectKey() {
        return profileImageObjectKey;
    }

    public void setProfileImageObjectKey(String profileImageObjectKey) {
        this.profileImageObjectKey = profileImageObjectKey;
    }
}