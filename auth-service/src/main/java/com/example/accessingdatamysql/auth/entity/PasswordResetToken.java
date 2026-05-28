package com.example.accessingdatamysql.auth.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entity representing a single-use token used to reset a user's password.
 *
 * <p>The raw token is never stored. Only a SHA-256 hash of the token is
 * persisted in {@link #tokenHash}, which means a leak of the database does
 * not expose usable reset links. The raw token is delivered to the user
 * exactly once, inside the reset email.</p>
 *
 * <p>A token is valid only when it has not yet been used ({@link #used} is
 * {@code false}) and the current time is before {@link #expiresAt}.</p>
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String tokenHash,
                              Integer userId,
                              Instant createdAt,
                              Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}