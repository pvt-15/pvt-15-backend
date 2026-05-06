package com.example.accessingdatamysql.user.enums;

public enum Level {
    LEVEL_1(0, 1, "Lövjägare"),
    LEVEL_2(150, 2, "Djurspanare"),
    LEVEL_3(300, 3, "Skogsexpert"),
    LEVEL_4(600, 4, "Naturmästare");

    private final int minPoints;
    private final int code;
    private final String type;

    Level(int minPoints, int code, String type) {
        this.minPoints = minPoints;
        this.code = code;
        this.type = type;
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

    public String getType() {
        return type;
    }
}