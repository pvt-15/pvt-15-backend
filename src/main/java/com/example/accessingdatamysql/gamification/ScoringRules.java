package com.example.accessingdatamysql.gamification;

import com.example.accessingdatamysql.model.challenge.enums.ChallengeDifficulty;

public final class ScoringRules {

    private ScoringRules() {
    }

    public static final int NEW_UNIQUE_DISCOVERY_POINTS = 10;
    public static final int DISCOVERY_MILESTONE_SIZE = 10;
    public static final int DISCOVERY_MILESTONE_BONUS = 50;

    public static final int CHALLENGE_REWARD = 100;

    public static int defaultChallengeReward(ChallengeDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 100;
            case MEDIUM -> 150;
            case HARD -> 200;
        };
    }
}