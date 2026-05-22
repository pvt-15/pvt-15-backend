package com.example.accessingdatamysql.user.service;

import com.example.accessingdatamysql.storage.StorageServiceClient;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StorageServiceClient storageServiceClient;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            StorageServiceClient storageServiceClient
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.storageServiceClient = storageServiceClient;
    }

    public UserResponse getCurrentUser(Integer userId, String jwtToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        String signedProfileImageUrl = storageServiceClient.generateSignedReadUrl(
                user.getProfileImageObjectKey(),
                jwtToken
        );

        return userMapper.toUserResponse(user, signedProfileImageUrl);
    }

    @Transactional
    public void deleteCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        userRepository.delete(user);
    }
}