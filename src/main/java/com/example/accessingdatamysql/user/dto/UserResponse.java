package com.example.accessingdatamysql.user.dto;

import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.Provider;

public class UserResponse {

    private Integer id;
    private String name;
    private String email;
    private Provider provider;
    private String providerUserId;
    private int totalPoints;
    private Level level;

    public UserResponse(Integer id,
                        String name,
                        String email,
                        Provider provider,
                        String providerUserId,
                        int totalPoints,
                        Level level) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerUserId = providerUserId;
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
}
