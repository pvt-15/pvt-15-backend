package com.example.accessingdatamysql.achievement.entity;

import com.example.accessingdatamysql.achievement.enums.BadgeTier;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import jakarta.persistence.*;

@Entity
public class BadgeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private PictureCategory category;

    @Enumerated(EnumType.STRING)
    private BadgeTier tier;

    private Integer requiredCount;
    private boolean active;

    public BadgeDefinition() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PictureCategory getCategory() {
        return category;
    }

    public void setCategory(PictureCategory category) {
        this.category = category;
    }

    public BadgeTier getTier() {
        return tier;
    }

    public void setTier(BadgeTier tier) {
        this.tier = tier;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
