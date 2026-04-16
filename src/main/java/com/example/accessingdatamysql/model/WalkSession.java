package com.example.accessingdatamysql.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Date;

public class WalkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int userId;
    private int forestAreaId;
    private int totalPointsEarned;
    private Date date;

    public WalkSession(int id,
                       int userId,
                       int forestAreaId,
                       int totalPointsEarned,
                       Date date){
        this.id = id;
        this.userId = userId;
        this.forestAreaId = forestAreaId;
        this.totalPointsEarned = totalPointsEarned;
        this.date = date;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public int getForestAreaId() {
        return forestAreaId;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public Date getDate() {
        return date;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setForestAreaId(int forestAreaId) {
        this.forestAreaId = forestAreaId;
    }

    public void setTotalPointsEarned(int totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

