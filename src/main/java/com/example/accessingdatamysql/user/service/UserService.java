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
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final PictureRepository pictureRepository;
    private final UserDiscoveryRepository userDiscoveryRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserChallengeProgressRepository userChallengeProgressRepository;
    private final UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;
    private final UserChallengePictureMatchRepository userChallengePictureMatchRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;

    public UserService(UserRepository userRepository,
                       PictureRepository pictureRepository,
                       UserDiscoveryRepository userDiscoveryRepository,
                       UserBadgeRepository userBadgeRepository,
                       UserChallengeProgressRepository userChallengeProgressRepository,
                       UserChallengeTaskProgressRepository userChallengeTaskProgressRepository,
                       UserChallengePictureMatchRepository userChallengePictureMatchRepository,
                       UserQuizAttemptRepository userQuizAttemptRepository,
                       UserQuizAnswerRepository userQuizAnswerRepository) {
        this.userRepository = userRepository;
        this.pictureRepository = pictureRepository;
        this.userDiscoveryRepository = userDiscoveryRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userChallengeProgressRepository = userChallengeProgressRepository;
        this.userChallengeTaskProgressRepository = userChallengeTaskProgressRepository;
        this.userChallengePictureMatchRepository = userChallengePictureMatchRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.userQuizAnswerRepository = userQuizAnswerRepository;
    }

    @Transactional
    public void deleteCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        deleteChallengeData(user);
        deleteQuizData(user);
        deleteBadges(user);
        deleteDiscoveries(user);
        deletePictures(user);

        userRepository.delete(user);
    }

    private void deleteChallengeData(User user) {
        List<UserChallengeProgress> progressList = userChallengeProgressRepository.findByUser(user);

        for (UserChallengeProgress progress : progressList) {
            List<UserChallengePictureMatch> pictureMatches =
                    userChallengePictureMatchRepository.findByTaskProgress_UserChallengeProgress(progress);

            userChallengePictureMatchRepository.deleteAll(pictureMatches);

            List<UserChallengeTaskProgress> taskProgressList =
                    userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

            userChallengeTaskProgressRepository.deleteAll(taskProgressList);
        }

        userChallengeProgressRepository.deleteAll(progressList);
    }

    private void deleteQuizData(User user) {
        List<UserQuizAttempt> attempts = userQuizAttemptRepository.findByUser(user);

        for (UserQuizAttempt attempt : attempts) {
            List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserQuizAttempt(attempt);
            userQuizAnswerRepository.deleteAll(answers);
        }

        userQuizAttemptRepository.deleteAll(attempts);
    }

    private void deleteBadges(User user) {
        List<UserBadge> badges = userBadgeRepository.findByUser(user);
        userBadgeRepository.deleteAll(badges);
    }

    private void deleteDiscoveries(User user) {
        List<UserDiscovery> discoveries = userDiscoveryRepository.findByUser(user);
        userDiscoveryRepository.deleteAll(discoveries);
    }

    private void deletePictures(User user) {
        List<Picture> pictures = pictureRepository.findByUser(user);
        pictureRepository.deleteAll(pictures);
    }
}