package com.example.accessingdatamysql.achievement.dto;

public class BadgeResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String tier;
    private Integer requiredCount;
    private String unlockedAt;
    private String imageUrl;

    public BadgeResponse() {
    }

    public BadgeResponse(Integer id,
                         String code,
                         String name,
                         String description,
                         String category,
                         String tier,
                         Integer requiredCount,
                         String unlockedAt,
                         String imageUrl) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tier = tier;
        this.requiredCount = requiredCount;
        this.unlockedAt = unlockedAt;
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
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

    public String getUnlockedAt() {
        return unlockedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}