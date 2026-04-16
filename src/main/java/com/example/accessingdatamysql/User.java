package com.example.accessingdatamysql;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;
    private String email;
    private String passwordHash;
    private int totalPoints;
    private Level level;

    // Constructors
    public User() {
    }

    public User(String name,
                String email,
                String passwordHash,
                int totalPoints,
                Level level){
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.totalPoints = totalPoints;
        this.level = level;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public Level getLevel() {
        return level;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
