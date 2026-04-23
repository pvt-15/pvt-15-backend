package com.example.accessingdatamysql.model.challenge.entity;

import jakarta.persistence.*;

@Entity
public class UserChallengeTaskProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private UserChallengeProgress userChallengeProgress;

    @ManyToOne
    private ChallengeTask challengeTask;

    private Integer currentCount;
    private boolean completed;

    public UserChallengeTaskProgress() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserChallengeProgress getUserChallengeProgress() {
        return userChallengeProgress;
    }

    public void setUserChallengeProgress(UserChallengeProgress userChallengeProgress) {
        this.userChallengeProgress = userChallengeProgress;
    }

    public ChallengeTask getChallengeTask() {
        return challengeTask;
    }

    public void setChallengeTask(ChallengeTask challengeTask) {
        this.challengeTask = challengeTask;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}