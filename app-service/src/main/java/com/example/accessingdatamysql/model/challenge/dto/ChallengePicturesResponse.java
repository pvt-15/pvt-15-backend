package com.example.accessingdatamysql.model.challenge.dto;

import java.util.List;

public class ChallengePicturesResponse {

    private Integer challengeId;
    private String title;
    private String status;
    private List<ChallengeTaskPicturesResponse> tasks;

    public ChallengePicturesResponse(Integer challengeId,
                                     String title,
                                     String status,
                                     List<ChallengeTaskPicturesResponse> tasks) {
        this.challengeId = challengeId;
        this.title = title;
        this.status = status;
        this.tasks = tasks;
    }

    public Integer getChallengeId() {
        return challengeId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public List<ChallengeTaskPicturesResponse> getTasks() {
        return tasks;
    }
}