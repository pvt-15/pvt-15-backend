package com.example.accessingdatamysql.storage.enums;

public enum ProfileImagePreset {

    BADGER("badger", "profile-images/presets/badger.png"),
    BEAR("bear", "profile-images/presets/bear.png"),
    FOX("fox", "profile-images/presets/fox.png"),
    MOOSE("moose", "profile-images/presets/moose.png"),
    MOUSE("mouse", "profile-images/presets/mouse.png"),
    SNAKE("snake", "profile-images/presets/snake.png");

    private final String avatarId;
    private final String objectKey;

    ProfileImagePreset(String avatarId, String objectKey) {
        this.avatarId = avatarId;
        this.objectKey = objectKey;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getObjectKey() {
        return objectKey;
    }
}