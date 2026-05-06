package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;

public class CreatePictureRequest {

    private String imageObjectKey;
    private TargetType targetType;
    private PictureMode pictureMode;

    public CreatePictureRequest() {
    }

    public CreatePictureRequest(String imageObjectKey, TargetType targetType, PictureMode pictureMode) {
        this.imageObjectKey = imageObjectKey;
        this.targetType = targetType;
        this.pictureMode = pictureMode;
    }

    public String getImageObjectKey() {
        return imageObjectKey;
    }

    public void setImageObjectKey(String imageObjectKey) {
        this.imageObjectKey = imageObjectKey;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public PictureMode getPictureMode() {
        return pictureMode;
    }

    public void setPictureMode(PictureMode pictureMode) {
        this.pictureMode = pictureMode;
    }
}