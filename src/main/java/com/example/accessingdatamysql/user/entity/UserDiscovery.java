package com.example.accessingdatamysql.user.entity;

import com.example.accessingdatamysql.picture.enums.PictureCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserDiscovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private PictureCategory category;

    private String normalizedLabel;
    private LocalDateTime discoveredAt;

    public UserDiscovery(){

    }

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public PictureCategory getCategory() {
        return category;
    }

    public String getNormalizedLabel() {
        return normalizedLabel;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCategory(PictureCategory category) {
        this.category = category;
    }

    public void setNormalizedLabel(String normalizedLabel) {
        this.normalizedLabel = normalizedLabel;
    }

    public void setDiscoveredAt(LocalDateTime discoveredAt) {
        this.discoveredAt = discoveredAt;
    }
}
