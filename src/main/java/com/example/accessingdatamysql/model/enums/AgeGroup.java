package com.example.accessingdatamysql.model.enums;

public enum AgeGroup {
    GROUP_ONE(2, 3),
    GROUP_TWO(4, 5),
    GROUP_THREE(6, 7);

    private int from;
    private int to;

    AgeGroup(int from, int to){
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String toString(){
        return from + " - " + to;
    }
}
