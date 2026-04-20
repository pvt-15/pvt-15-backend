package com.example.accessingdatamysql.picture.dto;

public class CreatePictureRequest {

    private String label;
    private String category;
    private double aiConfidence;
    private String imageUrl;

    public CreatePictureRequest(){

    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public double getAiConfidence() {
        return aiConfidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAiConfidence(double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

