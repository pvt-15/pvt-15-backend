package com.example.accessingdatamysql.storage.client;

public class SignedUrlResponse {

    private String imageUrl;

    public SignedUrlResponse() {
    }

    public SignedUrlResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}