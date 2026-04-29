package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeTaskResponse {

    private Integer id;
    private String taskText;
    private String taskType;
    private String requiredLabel;
    private String requiredCategory;
    private Integer requiredCount;
    private boolean mustBeUnique;
    private String referenceImageUrl;
    private String helpText;

    public ChallengeTaskResponse() {
    }

    public ChallengeTaskResponse(Integer id,
                                 String taskText,
                                 String taskType,
                                 String requiredLabel,
                                 String requiredCategory,
                                 Integer requiredCount,
                                 boolean mustBeUnique,
                                 String referenceImageUrl,
                                 String helpText) {
        this.id = id;
        this.taskText = taskText;
        this.taskType = taskType;
        this.requiredLabel = requiredLabel;
        this.requiredCategory = requiredCategory;
        this.requiredCount = requiredCount;
        this.mustBeUnique = mustBeUnique;
        this.referenceImageUrl = referenceImageUrl;
        this.helpText = helpText;
    }

    public Integer getId() {
        return id;
    }

    public String getTaskText() {
        return taskText;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getRequiredLabel() {
        return requiredLabel;
    }

    public String getRequiredCategory() {
        return requiredCategory;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public boolean isMustBeUnique() {
        return mustBeUnique;
    }

    public String getReferenceImageUrl() {
        return referenceImageUrl;
    }

    public String getHelpText() {
        return helpText;
    }
}