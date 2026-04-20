package com.example.accessingdatamysql.picture.dto;

public class CreatePictureRequest {

    private String imageUrl;

    public CreatePictureRequest() {
    }

    public CreatePictureRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}