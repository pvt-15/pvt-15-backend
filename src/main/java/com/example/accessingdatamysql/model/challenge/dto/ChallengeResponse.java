package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeResponse {

    private Integer id;
    private String title;
    private String description;
    private String type;
    private String difficulty;
    private Integer rewardPoints;
    private boolean active;
    private String status;

    public ChallengeResponse() {
    }

    public ChallengeResponse(Integer id,
                             String title,
                             String description,
                             String type,
                             String difficulty,
                             Integer rewardPoints,
                             boolean active,
                             String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.rewardPoints = rewardPoints;
        this.active = active;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatus() {
        return status;
    }
}