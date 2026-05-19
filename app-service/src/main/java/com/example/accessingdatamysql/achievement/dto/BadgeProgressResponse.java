package com.example.accessingdatamysql.achievement.dto;

public class BadgeProgressResponse {

    private Integer badgeDefinitionId;
    private Integer userBadgeId;
    private String code;
    private String name;
    private String description;
    private String category;
    private String tier;
    private Integer requiredCount;
    private Long currentCount;
    private boolean unlocked;
    private String unlockedAt;
    private String imageUrl;

    public BadgeProgressResponse() {
    }

    public BadgeProgressResponse(Integer badgeDefinitionId,
                                 Integer userBadgeId,
                                 String code,
                                 String name,
                                 String description,
                                 String category,
                                 String tier,
                                 Integer requiredCount,
                                 Long currentCount,
                                 boolean unlocked,
                                 String unlockedAt,
                                 String imageUrl) {
        this.badgeDefinitionId = badgeDefinitionId;
        this.userBadgeId = userBadgeId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tier = tier;
        this.requiredCount = requiredCount;
        this.currentCount = currentCount;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
        this.imageUrl = imageUrl;
    }

    public Integer getBadgeDefinitionId() {
        return badgeDefinitionId;
    }

    public Integer getUserBadgeId() {
        return userBadgeId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getTier() {
        return tier;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public Long getCurrentCount() {
        return currentCount;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public String getUnlockedAt() {
        return unlockedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}