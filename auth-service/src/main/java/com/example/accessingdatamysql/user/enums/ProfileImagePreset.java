package com.example.accessingdatamysql.user.enums;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<ProfileImagePreset> fromAvatarId(String avatarId) {
        if (avatarId == null || avatarId.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(preset -> preset.avatarId.equalsIgnoreCase(avatarId.trim()))
                .findFirst();
    }

    public static boolean isAllowedObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }

        return Arrays.stream(values())
                .anyMatch(preset -> preset.objectKey.equals(objectKey.trim()));
    }
}