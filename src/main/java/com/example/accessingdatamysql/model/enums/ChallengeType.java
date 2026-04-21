package com.example.accessingdatamysql.model.enums;

public enum ChallengeType {
    TYPE_ONE(1, "one"),
    TYPE_TWO(2, "two"),
    TYPE_THREE(3, "three"),
    TYPE_FOUR(4, "four"),
    TYPE_FIVE(5, "five"),
    TYPE_SIX(6, "six");

    private final int code;
    private final String type;

    ChallengeType(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
