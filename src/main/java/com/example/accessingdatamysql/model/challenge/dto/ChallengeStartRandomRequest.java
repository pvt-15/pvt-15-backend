package com.example.accessingdatamysql.model.challenge.dto;

public class ChallengeStartRandomRequest {

    private String challengeDifficulty;
    private String challengeType;

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
}