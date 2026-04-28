package com.example.accessingdatamysql.model.challenge.dto;

import java.util.List;

public class ChallengeCreateRequest {

    private String title;
    private String description;
    private String type;
    private String difficulty;
    private Integer rewardPoints;
    private boolean active;
    private Integer startMonth;
    private Integer endMonth;
    private String locationName;
    private List<ChallengeTaskCreateRequest> tasks;

    public ChallengeCreateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Integer endMonth) {
        this.endMonth = endMonth;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<ChallengeTaskCreateRequest> getTasks() {
        return tasks;
    }

    public void setTasks(List<ChallengeTaskCreateRequest> tasks) {
        this.tasks = tasks;
    }
}
