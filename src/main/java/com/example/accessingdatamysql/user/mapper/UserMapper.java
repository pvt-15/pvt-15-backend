package com.example.accessingdatamysql.user.mapper;

import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
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
                user.getLevel());
    }
}
