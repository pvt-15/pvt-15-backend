package com.example.accessingdatamysql.picture.dto;

public class PictureStatsResponse {

    private int totalPictures;
    private int totalPlants;
    private int totalAnimals;
    private int totalFlowers;
    private int totalTrees;
    private int totalBirds;
    private int totalInsects;
    private int totalPoints;

    public PictureStatsResponse() {
    }

    public PictureStatsResponse(int totalPictures,
                                int totalPlants,
                                int totalAnimals,
                                int totalFlowers,
                                int totalTrees,
                                int totalBirds,
                                int totalInsects,
                                int totalPoints) {
        this.totalPictures = totalPictures;
        this.totalPlants = totalPlants;
        this.totalAnimals = totalAnimals;
        this.totalFlowers = totalFlowers;
        this.totalTrees = totalTrees;
        this.totalBirds = totalBirds;
        this.totalInsects = totalInsects;
        this.totalPoints = totalPoints;
    }

    public int getTotalPictures() {
        return totalPictures;
    }

    public void setTotalPictures(int totalPictures) {
        this.totalPictures = totalPictures;
    }

    public int getTotalPlants() {
        return totalPlants;
    }

    public void setTotalPlants(int totalPlants) {
        this.totalPlants = totalPlants;
    }

    public int getTotalAnimals() {
        return totalAnimals;
    }

    public void setTotalAnimals(int totalAnimals) {
        this.totalAnimals = totalAnimals;
    }

    public int getTotalFlowers() {
        return totalFlowers;
    }

    public void setTotalFlowers(int totalFlowers) {
        this.totalFlowers = totalFlowers;
    }

    public int getTotalTrees() {
        return totalTrees;
    }

    public void setTotalTrees(int totalTrees) {
        this.totalTrees = totalTrees;
    }

    public int getTotalBirds() {
        return totalBirds;
    }

    public void setTotalBirds(int totalBirds) {
        this.totalBirds = totalBirds;
    }

    public int getTotalInsects() {
        return totalInsects;
    }

    public void setTotalInsects(int totalInsects) {
        this.totalInsects = totalInsects;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}