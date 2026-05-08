package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengePictureMatchRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeTaskProgressRepository;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAnswerRepository;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAttemptRepository;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import com.example.accessingdatamysql.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private UserDiscoveryRepository userDiscoveryRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private UserChallengeProgressRepository userChallengeProgressRepository;

    @Mock
    private UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;

    @Mock
    private UserChallengePictureMatchRepository userChallengePictureMatchRepository;

    @Mock
    private UserQuizAttemptRepository userQuizAttemptRepository;

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteCurrentUser_shouldDeleteUserAndRelatedData() {
        User user = new User();
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userChallengeProgressRepository.findByUser(user)).thenReturn(List.of());
        when(userQuizAttemptRepository.findByUser(user)).thenReturn(List.of());
        when(userBadgeRepository.findByUser(user)).thenReturn(List.of());
        when(userDiscoveryRepository.findByUser(user)).thenReturn(List.of());
        when(pictureRepository.findByUser(user)).thenReturn(List.of());

        userService.deleteCurrentUser(1);

        verify(userRepository).delete(user);
    }
}