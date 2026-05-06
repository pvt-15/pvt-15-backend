package com.example.accessingdatamysql.challenge.service;

import com.example.accessingdatamysql.model.challenge.dto.ChallengeResponse;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeStartRandomRequest;
import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import com.example.accessingdatamysql.model.challenge.entity.ChallengeTask;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeDifficulty;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeType;
import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.model.challenge.repository.ChallengeRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeTaskProgressRepository;
import com.example.accessingdatamysql.model.challenge.service.ChallengeService;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChallengeProgressRepository userChallengeProgressRepository;

    @Mock
    private UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;

    @InjectMocks
    private ChallengeService challengeService;

    @Test
    void startChallenge_shouldCreateTaskProgressRowsForAllTasks() {
        User user = new User();
        user.setId(1);

        Challenge challenge = new Challenge();
        challenge.setId(4);
        challenge.setTitle("Humlegården");
        challenge.setDescription("Flowers and bumblebee");
        challenge.setType(ChallengeType.LOCATION);
        challenge.setDifficulty(ChallengeDifficulty.MEDIUM);
        challenge.setRewardPoints(175);
        challenge.setActive(true);
        challenge.setTasks(new ArrayList<>());

        ChallengeTask task1 = new ChallengeTask();
        task1.setId(10);
        task1.setTaskText("Hitta 2 olika blommor");
        task1.setTaskType(TaskType.CATEGORY);
        task1.setRequiredCategory(PictureCategory.FLOWER);
        task1.setRequiredCount(2);
        task1.setMustBeUnique(true);
        task1.setChallenge(challenge);

        ChallengeTask task2 = new ChallengeTask();
        task2.setId(11);
        task2.setTaskText("Hitta en humla");
        task2.setTaskType(TaskType.LABEL);
        task2.setRequiredLabel("bumblebee");
        task2.setRequiredCount(1);
        task2.setMustBeUnique(false);
        task2.setChallenge(challenge);

        challenge.getTasks().add(task1);
        challenge.getTasks().add(task2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(challengeRepository.findById(4)).thenReturn(Optional.of(challenge));
        when(userChallengeProgressRepository.findByUserAndChallenge(user, challenge))
                .thenReturn(Optional.empty());
        when(userChallengeProgressRepository.save(any(UserChallengeProgress.class)))
                .thenAnswer(invocation -> {
                    UserChallengeProgress progress = invocation.getArgument(0);
                    progress.setId(99);
                    return progress;
                });

        ChallengeResponse response = challengeService.startChallenge(1, 4);

        assertEquals("IN_PROGRESS", response.getStatus());
        verify(userChallengeProgressRepository).save(any(UserChallengeProgress.class));
        verify(userChallengeTaskProgressRepository, times(2)).save(any(UserChallengeTaskProgress.class));
    }

    @Test
    void startChallenge_shouldBackfillTaskProgressIfProgressExistsButTasksAreMissing() {
        User user = new User();
        user.setId(1);

        Challenge challenge = new Challenge();
        challenge.setId(4);
        challenge.setTitle("Humlegården");
        challenge.setDescription("Flowers and bumblebee");
        challenge.setType(ChallengeType.LOCATION);
        challenge.setDifficulty(ChallengeDifficulty.MEDIUM);
        challenge.setRewardPoints(175);
        challenge.setActive(true);
        challenge.setTasks(new ArrayList<>());

        ChallengeTask task1 = new ChallengeTask();
        task1.setId(10);
        task1.setTaskText("Hitta 2 olika blommor");
        task1.setTaskType(TaskType.CATEGORY);
        task1.setRequiredCategory(PictureCategory.FLOWER);
        task1.setRequiredCount(2);
        task1.setMustBeUnique(true);
        task1.setChallenge(challenge);

        ChallengeTask task2 = new ChallengeTask();
        task2.setId(11);
        task2.setTaskText("Hitta en humla");
        task2.setTaskType(TaskType.LABEL);
        task2.setRequiredLabel("bumblebee");
        task2.setRequiredCount(1);
        task2.setMustBeUnique(false);
        task2.setChallenge(challenge);

        challenge.getTasks().add(task1);
        challenge.getTasks().add(task2);

        UserChallengeProgress existingProgress = new UserChallengeProgress();
        existingProgress.setId(50);
        existingProgress.setUser(user);
        existingProgress.setChallenge(challenge);
        existingProgress.setStatus(ChallengeStatus.IN_PROGRESS);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(challengeRepository.findById(4)).thenReturn(Optional.of(challenge));
        when(userChallengeProgressRepository.findByUserAndChallenge(user, challenge))
                .thenReturn(Optional.of(existingProgress));
        when(userChallengeTaskProgressRepository.findByUserChallengeProgress(existingProgress))
                .thenReturn(Collections.emptyList());

        ChallengeResponse response = challengeService.startChallenge(1, 4);

        assertEquals("IN_PROGRESS", response.getStatus());
        verify(userChallengeTaskProgressRepository, times(2)).save(any(UserChallengeTaskProgress.class));
        verify(userChallengeProgressRepository, never()).save(any(UserChallengeProgress.class));
    }

    @Test
    void startRandomChallenge_withCategory_shouldFilterByDifficultyTypeAndCategory() {
        User user = new User();
        user.setId(1);

        Challenge challenge = new Challenge();
        challenge.setId(10);
        challenge.setTitle("Trädbingo");
        challenge.setDescription("Hitta träd");
        challenge.setType(ChallengeType.BINGO);
        challenge.setDifficulty(ChallengeDifficulty.HARD);
        challenge.setCategory(PictureCategory.TREE);
        challenge.setRewardPoints(200);
        challenge.setActive(true);

        ChallengeStartRandomRequest request = new ChallengeStartRandomRequest();
        request.setChallengeDifficulty("HARD");
        request.setChallengeType("BINGO");
        request.setChallengeCategory("TREE");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(challengeRepository.findByActiveTrueAndDifficultyAndTypeAndCategory(
                ChallengeDifficulty.HARD,
                ChallengeType.BINGO,
                PictureCategory.TREE
        )).thenReturn(List.of(challenge));
        when(userChallengeProgressRepository.findByUserAndChallenge(user, challenge))
                .thenReturn(Optional.empty());
        when(challengeRepository.findById(10)).thenReturn(Optional.of(challenge));
        when(userChallengeProgressRepository.save(any(UserChallengeProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChallengeResponse response = challengeService.startRandomChallenge(1, request);

        assertEquals("Trädbingo", response.getTitle());

        verify(challengeRepository).findByActiveTrueAndDifficultyAndTypeAndCategory(
                ChallengeDifficulty.HARD,
                ChallengeType.BINGO,
                PictureCategory.TREE
        );
        verify(challengeRepository, never()).findByActiveTrueAndDifficultyAndType(
                any(),
                any()
        );
    }

    @Test
    void startRandomChallenge_withoutCategory_shouldFilterOnlyByDifficultyAndType() {
        User user = new User();
        user.setId(1);

        Challenge challenge = new Challenge();
        challenge.setId(11);
        challenge.setTitle("Random bingo");
        challenge.setDescription("Valfri bingo");
        challenge.setType(ChallengeType.BINGO);
        challenge.setDifficulty(ChallengeDifficulty.EASY);
        challenge.setRewardPoints(100);
        challenge.setActive(true);

        ChallengeStartRandomRequest request = new ChallengeStartRandomRequest();
        request.setChallengeDifficulty("EASY");
        request.setChallengeType("BINGO");
        request.setChallengeCategory(null);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(challengeRepository.findByActiveTrueAndDifficultyAndType(
                ChallengeDifficulty.EASY,
                ChallengeType.BINGO
        )).thenReturn(List.of(challenge));
        when(userChallengeProgressRepository.findByUserAndChallenge(user, challenge))
                .thenReturn(Optional.empty());
        when(challengeRepository.findById(11)).thenReturn(Optional.of(challenge));
        when(userChallengeProgressRepository.save(any(UserChallengeProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChallengeResponse response = challengeService.startRandomChallenge(1, request);

        assertEquals("Random bingo", response.getTitle());

        verify(challengeRepository).findByActiveTrueAndDifficultyAndType(
                ChallengeDifficulty.EASY,
                ChallengeType.BINGO
        );
        verify(challengeRepository, never()).findByActiveTrueAndDifficultyAndTypeAndCategory(
                any(),
                any(),
                any()
        );
    }
}