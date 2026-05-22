package com.example.accessingdatamysql.user.mapper;

import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return toUserResponse(user, user.getProfileImageUrl());
    }

    public UserResponse toUserResponse(User user, String profileImageUrl) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getProviderUserId(),
                user.getTotalPoints(),
                user.getLevel().getDisplayName(),
                profileImageUrl,
                user.getProfileImageObjectKey()
        );
    }
}