package com.example.accessingdatamysql;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String title;
    private String description;
    private int points;
    private AgeGroup ageGroup;
    private ChallengeType challengeType;

    // Constructors
    public Challenge(String title,
                     String description,
                     int points,
                     AgeGroup ageGroup,
                     ChallengeType challengeType){
        this.title = title;
        this.description = description;
        this.points = points;
        this.ageGroup = ageGroup;
        this.challengeType = challengeType;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public ChallengeType getChallengeType() {
        return challengeType;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
    }
}
