package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.picture.model.enums.TargetType;

public class CreatePictureRequest {

    private String imageUrl;
    private TargetType targetType;

    public CreatePictureRequest() {
    }

    public CreatePictureRequest(String imageUrl,
                                TargetType targetType) {
        this.imageUrl = imageUrl;
        this.targetType = targetType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
}