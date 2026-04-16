package com.example.accessingdatamysql;

public enum Level {
    LEVEL_1(1, "Lövjägare"),
    LEVEL_2(2, "Djurspanare"),
    LEVEL_3(3, "Skogsexpert");

    private final int code;
    private final String type;

    Level(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
