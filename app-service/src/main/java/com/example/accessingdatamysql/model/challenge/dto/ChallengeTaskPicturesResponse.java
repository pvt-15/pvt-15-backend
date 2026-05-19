package com.example.accessingdatamysql.model.challenge.dto;

import java.util.List;

public class ChallengeTaskPicturesResponse {

    private Integer taskProgressId;
    private Integer taskId;
    private String taskText;
    private Integer currentCount;
    private Integer requiredCount;
    private boolean completed;
    private List<ChallengeMatchedPictureResponse> pictures;

    public ChallengeTaskPicturesResponse(Integer taskProgressId,
                                         Integer taskId,
                                         String taskText,
                                         Integer currentCount,
                                         Integer requiredCount,
                                         boolean completed,
                                         List<ChallengeMatchedPictureResponse> pictures) {
        this.taskProgressId = taskProgressId;
        this.taskId = taskId;
        this.taskText = taskText;
        this.currentCount = currentCount;
        this.requiredCount = requiredCount;
        this.completed = completed;
        this.pictures = pictures;
    }

    public Integer getTaskProgressId() {
        return taskProgressId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public String getTaskText() {
        return taskText;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public List<ChallengeMatchedPictureResponse> getPictures() {
        return pictures;
    }
}