package com.example.accessingdatamysql.user.entity;

import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.auth.enums.Provider;
import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerUserId;

    private int totalPoints;

    @Enumerated(EnumType.STRING)
    private Level level;

    public User() {
    }

    public User(String name, String email, String passwordHash, Provider provider, int totalPoints, Level level) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = provider;
        this.totalPoints = totalPoints;
        this.level = level;
    }

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

    public Provider getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public Level getLevel() {
        return level;
    }

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

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}