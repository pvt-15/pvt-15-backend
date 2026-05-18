package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeStartRandomRequest {

    private String challengeDifficulty;
    private String challengeType;
    private String challengeCategory;

    public ChallengeStartRandomRequest() {
    }

    public String getChallengeDifficulty() {
        return challengeDifficulty;
    }

    public void setChallengeDifficulty(String challengeDifficulty) {
        this.challengeDifficulty = challengeDifficulty;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }

    public String getChallengeCategory() {
        return challengeCategory;
    }

    public void setChallengeCategory(String challengeCategory) {
        this.challengeCategory = challengeCategory;
    }
}