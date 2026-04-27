package com.example.accessingdatamysql.picture.dto;

public class DiscoveryCategoryStatsResponse {

    private String category;
    private long uniqueCount;
    private long nextMilestone;
    private long remainingToNextMilestone;

    public DiscoveryCategoryStatsResponse(){

    }

    public DiscoveryCategoryStatsResponse(String category,
                                          long uniqueCount,
                                          long nextMilestone,
                                          long remainingToNextMilestone){
        this.category = category;
        this.uniqueCount = uniqueCount;
        this.nextMilestone = nextMilestone;
        this.remainingToNextMilestone = remainingToNextMilestone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getUniqueCount() {
        return uniqueCount;
    }

    public void setUniqueCount(long uniqueCount) {
        this.uniqueCount = uniqueCount;
    }

    public long getNextMilestone() {
        return nextMilestone;
    }

    public void setNextMilestone(long nextMilestone) {
        this.nextMilestone = nextMilestone;
    }

    public long getRemainingToNextMilestone() {
        return remainingToNextMilestone;
    }

    public void setRemainingToNextMilestone(long remainingToNextMilestone) {
        this.remainingToNextMilestone = remainingToNextMilestone;
    }
}
