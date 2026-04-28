package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeTaskCreateRequest {

    private String taskText;
    private String taskType;
    private String requiredLabel;
    private String requiredCategory;
    private Integer requiredCount;
    private boolean mustBeUnique;

    public ChallengeTaskCreateRequest() {

    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getRequiredLabel() {
        return requiredLabel;
    }

    public void setRequiredLabel(String requiredLabel) {
        this.requiredLabel = requiredLabel;
    }

    public String getRequiredCategory() {
        return requiredCategory;
    }

    public void setRequiredCategory(String requiredCategory) {
        this.requiredCategory = requiredCategory;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }

    public boolean isMustBeUnique() {
        return mustBeUnique;
    }

    public void setMustBeUnique(boolean mustBeUnique) {
        this.mustBeUnique = mustBeUnique;
    }

}
