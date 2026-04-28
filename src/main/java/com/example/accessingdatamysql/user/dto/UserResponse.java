package com.example.accessingdatamysql.user.dto;

import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.auth.enums.Provider;

/**
 * DataTransferObject which represents userdata which is sent to the client.
 *
 * <p>UserResponse is used when the application wants to return userinfo without
 * exposing the internal {@code User}. It contains both basic identification data and
 * things like total points/level.</p>
 *
 * <ul>
 *     <li>user-ID</li>
 *     <li>name and email</li>
 *     <li>provider</li>
 *     <li>provider-specific user-ID, if applicable</li>
 *     <li>current total points</li>
 *     <li>current level</li>
 * </ul>
 */
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
