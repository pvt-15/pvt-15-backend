package com.example.accessingdatamysql.model.challenge.dto;

public class DailyChallengePictureRequest {

    private String imageObjectKey;

    //Only used if a daily challenge has multiple tasks
    private Integer taskId;

    public DailyChallengePictureRequest() {
    }

    public DailyChallengePictureRequest(String imageObjectKey, Integer taskId) {
        this.imageObjectKey = imageObjectKey;
        this.taskId = taskId;
    }

    public String getImageObjectKey() {
        return imageObjectKey;
    }

    public void setImageObjectKey(String imageObjectKey) {
        this.imageObjectKey = imageObjectKey;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}