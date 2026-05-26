package com.example.accessingdatamysql.model.challenge.dto;

public class DailyChallengePictureRequest {

    private String imageObjectKey;

    public DailyChallengePictureRequest() {
    }

    public DailyChallengePictureRequest(String imageObjectKey) {
        this.imageObjectKey = imageObjectKey;
    }

    public String getImageObjectKey() {
        return imageObjectKey;
    }

    public void setImageObjectKey(String imageObjectKey) {
        this.imageObjectKey = imageObjectKey;
    }
}