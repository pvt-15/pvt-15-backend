package com.example.accessingdatamysql.dto;

public class PictureResponse {

    private Integer id;
    private String label;
    private String category;
    private double aiConfidence;
    private int pointsAwarded;
    private String imageUrl;
    private String createdAt;

    public PictureResponse(Integer id,
                           String label,
                           String category,
                           double aiConfidence,
                           int pointsAwarded,
                           String imageUrl,
                           String createdAt){
        this.id = id;
        this.label = label;
        this.category = category;
        this.aiConfidence = aiConfidence;
        this.pointsAwarded = pointsAwarded;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
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

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
