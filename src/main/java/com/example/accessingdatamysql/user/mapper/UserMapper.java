package com.example.accessingdatamysql.user.mapper;

import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting a {@link User} to user-related DataTransferObject: {@link UserResponse}.
 *
 * <p>Used to ensure that only the necessary fields are returned
 * to the client</p>
 */
@Component
public class UserMapper {

    /**
     * Converts a {@code User}-entity to a {@code UserResponse}-object
     *
     * @param user user to be mapped
     * @return a {@code UserResponse}-object with the users public data,
     *         or {@code null} if parameter is {@code null}
     */
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
                user.getLevel()
        );
    }
}
