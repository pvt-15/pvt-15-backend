package com.example.accessingdatamysql.model.challenge.dto;

import java.util.List;

public class ChallengeDetailsResponse {

    private Integer id;
    private String title;
    private String description;
    private String type;
    private String difficulty;
    private Integer rewardPoints;
    private boolean active;
    private String locationName;
    private Integer startMonth;
    private Integer endMonth;
    private String status;
    private String category;
    private List<ChallengeTaskResponse> tasks;

    public ChallengeDetailsResponse() {
    }

    public ChallengeDetailsResponse(Integer id,
                                    String title,
                                    String description,
                                    String type,
                                    String difficulty,
                                    Integer rewardPoints,
                                    boolean active,
                                    String locationName,
                                    Integer startMonth,
                                    Integer endMonth,
                                    String status,
                                    String category,
                                    List<ChallengeTaskResponse> tasks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.rewardPoints = rewardPoints;
        this.active = active;
        this.locationName = locationName;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.status = status;
        this.tasks = tasks;
        this.category = category;
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

    public String getLocationName() {
        return locationName;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public List<ChallengeTaskResponse> getTasks() {
        return tasks;
    }
}