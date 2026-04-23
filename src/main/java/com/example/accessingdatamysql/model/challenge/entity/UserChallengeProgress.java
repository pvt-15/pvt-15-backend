package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public UserChallengeProgress() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}