package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;

public class CreatePictureRequest {

    private String imageObjectKey;;
    private TargetType targetType;
    private PictureMode pictureMode;

    public CreatePictureRequest() {
    }

    public CreatePictureRequest(String imageUrl,
                                TargetType targetType,
                                PictureMode pictureMode) {
        this.imageObjectKey = imageUrl;
        this.targetType = targetType;
        this.pictureMode = pictureMode;
    }

    public String getImageUrl() {
        return imageObjectKey;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public PictureMode getPictureMode() {
        return pictureMode;
    }

    public void setImageUrl(String imageUrl) {
        this.imageObjectKey = imageUrl;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public void setPictureMode(PictureMode pictureMode) {
        this.pictureMode = pictureMode;
    }
}