package com.example.accessingdatamysql.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class ForestArea {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;
    private String description;
    private String location;
    private int latitude;
    private int longitude;
    private int difficulty;
    private int ageRecommendation;

    // Constructors
    public ForestArea(String name,
                      String description,
                      String location,
                      int latitude,
                      int longitude,
                      int difficulty,
                      int ageRecommendation){
        this.name = name;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.difficulty = difficulty;
        this.ageRecommendation = ageRecommendation;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getAgeRecommendation() {
        return ageRecommendation;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setAgeRecommendation(int ageRecommendation) {
        this.ageRecommendation = ageRecommendation;
    }
}
