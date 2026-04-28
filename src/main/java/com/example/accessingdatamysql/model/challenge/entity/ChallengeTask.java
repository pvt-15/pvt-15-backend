package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import jakarta.persistence.*;

@Entity
public class ChallengeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Challenge challenge;

    private String taskText;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private String requiredLabel;

    @Enumerated(EnumType.STRING)
    private PictureCategory requiredCategory;

    private Integer requiredCount;
    private boolean mustBeUnique;

    public ChallengeTask() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getRequiredLabel() {
        return requiredLabel;
    }

    public void setRequiredLabel(String requiredLabel) {
        this.requiredLabel = requiredLabel;
    }

    public PictureCategory getRequiredCategory() {
        return requiredCategory;
    }

    public void setRequiredCategory(PictureCategory requiredCategory) {
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