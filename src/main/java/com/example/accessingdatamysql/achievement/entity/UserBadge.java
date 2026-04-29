package com.example.accessingdatamysql.achievement.entity;

import com.example.accessingdatamysql.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "badge_definition_id", nullable = false)
    private BadgeDefinition badgeDefinition;

    private LocalDateTime unlockedAt;

    public UserBadge() {
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

    public BadgeDefinition getBadgeDefinition() {
        return badgeDefinition;
    }

    public void setBadgeDefinition(BadgeDefinition badgeDefinition) {
        this.badgeDefinition = badgeDefinition;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}
