package com.example.accessingdatamysql.user.enums;

public enum Level {
    LEVEL_1(0, 1, "Skogsjägare"),
    LEVEL_2(300, 2, "Naturspanare"),
    LEVEL_3(1000, 3, "Skogsexpert"),
    LEVEL_4(2000, 4, "Skogsmästare"),
    LEVEL_5(1000000, 5, "Ludvigs världsherrevälde");

    private final int minPoints;
    private final int code;
    private final String displayName;

    Level(int minPoints, int code, String displayName) {
        this.minPoints = minPoints;
        this.code = code;
        this.displayName = displayName;
    }

    public static Level fromPoints(int totalPoints) {
        Level currentLevel = LEVEL_1;

        for (Level level : values()) {
            if (totalPoints >= level.minPoints) {
                currentLevel = level;
            }
        }

        return currentLevel;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}