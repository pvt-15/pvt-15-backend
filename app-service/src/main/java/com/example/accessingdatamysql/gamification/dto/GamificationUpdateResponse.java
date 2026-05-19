package com.example.accessingdatamysql.gamification.dto;

import com.example.accessingdatamysql.achievement.dto.BadgeResponse;

import java.util.List;

public class GamificationUpdateResponse {
    private boolean leveledUp;
    private String previousLevel;
    private String currentLevel;
    private List<BadgeResponse> newlyUnlockedBadges;

    public GamificationUpdateResponse(
            boolean leveledUp,
            String previousLevel,
            String currentLevel,
            List<BadgeResponse> newlyUnlockedBadges
    ) {
        this.leveledUp = leveledUp;
        this.previousLevel = previousLevel;
        this.currentLevel = currentLevel;
        this.newlyUnlockedBadges = newlyUnlockedBadges;
    }

    public boolean isLeveledUp() { return leveledUp; }
    public String getPreviousLevel() { return previousLevel; }
    public String getCurrentLevel() { return currentLevel; }
    public List<BadgeResponse> getNewlyUnlockedBadges() { return newlyUnlockedBadges; }
}