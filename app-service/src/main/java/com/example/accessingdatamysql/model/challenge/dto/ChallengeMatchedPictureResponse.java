package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeMatchedPictureResponse {

    private Integer pictureId;
    private String label;
    private String category;
    private String imageUrl;
    private String takenAt;

    public ChallengeMatchedPictureResponse(Integer pictureId,
                                           String label,
                                           String category,
                                           String imageUrl,
                                           String takenAt) {
        this.pictureId = pictureId;
        this.label = label;
        this.category = category;
        this.imageUrl = imageUrl;
        this.takenAt = takenAt;
    }

    public Integer getPictureId() {
        return pictureId;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTakenAt() {
        return takenAt;
    }
}