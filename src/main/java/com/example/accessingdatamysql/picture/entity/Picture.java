package com.example.accessingdatamysql.picture.entity;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String label;

    @Enumerated(EnumType.STRING)
    private PictureCategory category;

    private double aiConfidence;
    private int pointsAwarded;
    private String imageUrl;
    private LocalDateTime takenAt;

    @Enumerated(EnumType.STRING)
    private PictureMode pictureMode;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    public Picture() {
    }

    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public PictureCategory getCategory() {
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

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public PictureMode getPictureMode() {
        return pictureMode;
    }

    public User getUser() {
        return user;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCategory(PictureCategory category) {
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

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public void setPictureMode(PictureMode pictureMode) {
        this.pictureMode = pictureMode;
    }

    public void setUser(User user) {
        this.user = user;
    }
}