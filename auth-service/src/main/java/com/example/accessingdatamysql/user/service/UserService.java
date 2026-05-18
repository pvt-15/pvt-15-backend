package com.example.accessingdatamysql.user.service;

import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengePictureMatch;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengePictureMatchRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeTaskProgressRepository;
import com.example.accessingdatamysql.model.quiz.entity.UserQuizAnswer;
import com.example.accessingdatamysql.model.quiz.entity.UserQuizAttempt;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAnswerRepository;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAttemptRepository;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.entity.UserDiscovery;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        userRepository.delete(user);
    }
}