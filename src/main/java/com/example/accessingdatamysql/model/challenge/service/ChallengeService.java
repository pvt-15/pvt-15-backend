package com.example.accessingdatamysql.model.challenge.service;

import com.example.accessingdatamysql.model.challenge.dto.*;
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
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String CHALLENGE_NOT_FOUND = "Challenge not found";

    private static final String EMPTY_REQUEST_BODY = "Request body is required";
    private static final String EMPTY_CHALLENGE_TITLE = "Challenge title is required";
    private static final String EMPTY_CHALLENGE_TYPE = "Challenge type is required";
    private static final String EMPTY_CHALLENGE_DIFFICULTY = "Challenge difficulty is required";
    private static final String EMPTY_RANDOM_CHALLENGE_REQUEST = "Random challenge request is required";
    private static final String EMPTY_RANDOM_CHALLENGE_DIFFICULTY = "Challenge difficulty is required";
    private static final String EMPTY_RANDOM_CHALLENGE_TYPE = "Challenge type is required";
    private static final String NO_RANDOM_CHALLENGE_FOUND = "No available challenge found for selected difficulty and type";

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeProgressRepository userChallengeProgressRepository;
    private final UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;

    public ChallengeService(ChallengeRepository challengeRepository,
                            UserRepository userRepository,
                            UserChallengeProgressRepository userChallengeProgressRepository,
                            UserChallengeTaskProgressRepository userChallengeTaskProgressRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.userChallengeProgressRepository = userChallengeProgressRepository;
        this.userChallengeTaskProgressRepository = userChallengeTaskProgressRepository;
    }

    public List<ChallengeResponse> getActiveChallenges(Integer userId) {
        User user = getUserById(userId);
        List<Challenge> challenges = challengeRepository.findByActiveTrue();

        List<ChallengeResponse> responses = new ArrayList<>();
        for (Challenge challenge : challenges) {
            String status = getStatusForUser(user, challenge);
            responses.add(toChallengeResponse(challenge, status));
        }
        return responses;
    }

    @Transactional
    public ChallengeDetailsResponse getChallengeById(Integer userId, Integer challengeId) {
        User user = getUserById(userId);
        Challenge challenge = getChallengeByIdInternal(challengeId);

        String status = getStatusForUser(user, challenge);

        List<ChallengeTaskResponse> taskResponses = new ArrayList<>();
        for (ChallengeTask task : challenge.getTasks()) {
            taskResponses.add(toTaskResponse(task));
        }

        return new ChallengeDetailsResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getType().name(),
                challenge.getDifficulty().name(),
                challenge.getRewardPoints(),
                challenge.isActive(),
                challenge.getLocationName(),
                challenge.getStartMonth(),
                challenge.getEndMonth(),
                status,
                categoryNameOrNull(challenge),
                taskResponses
        );
    }

    @Transactional
    public ChallengeResponse startChallenge(Integer userId, Integer challengeId) {
        User user = getUserById(userId);
        Challenge challenge = getChallengeByIdInternal(challengeId);

        Optional<UserChallengeProgress> existing =
                userChallengeProgressRepository.findByUserAndChallenge(user, challenge);

        if (existing.isPresent()) {
            UserChallengeProgress progress = existing.get();

            List<UserChallengeTaskProgress> existingTaskProgress =
                    userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

            if (existingTaskProgress.isEmpty()) {
                createTaskProgress(challenge, progress);
            }

            return toChallengeResponse(challenge, progress.getStatus().name());
        }

        UserChallengeProgress progress = new UserChallengeProgress();
        progress.setUser(user);
        progress.setChallenge(challenge);
        progress.setStatus(ChallengeStatus.IN_PROGRESS);
        progress.setStartedAt(LocalDateTime.now());
        progress.setRewardClaimed(false);

        UserChallengeProgress savedProgress = userChallengeProgressRepository.save(progress);

        createTaskProgress(challenge, savedProgress);

        return toChallengeResponse(challenge, ChallengeStatus.IN_PROGRESS.name());
    }

    @Transactional
    public ChallengeDetailsResponse createChallenge(ChallengeCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(EMPTY_REQUEST_BODY);
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException(EMPTY_CHALLENGE_TITLE);
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException(EMPTY_CHALLENGE_TYPE);
        }
        if (request.getDifficulty() == null || request.getDifficulty().isBlank()) {
            throw new IllegalArgumentException(EMPTY_CHALLENGE_DIFFICULTY);
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(request.getTitle());
        challenge.setDescription(request.getDescription());
        challenge.setType(ChallengeType.valueOf(request.getType().toUpperCase()));
        challenge.setDifficulty(ChallengeDifficulty.valueOf(request.getDifficulty().toUpperCase()));
        challenge.setRewardPoints(request.getRewardPoints());
        challenge.setActive(request.isActive());
        challenge.setStartMonth(request.getStartMonth());
        challenge.setEndMonth(request.getEndMonth());
        challenge.setLocationName(request.getLocationName());

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            challenge.setCategory(
                    PictureCategory.valueOf(request.getCategory().toUpperCase())
            );
        }

        List<ChallengeTaskResponse> taskResponses = new ArrayList<>();

        if(request.getTasks() != null){
            for(ChallengeTaskCreateRequest taskRequest : request.getTasks()){
                ChallengeTask task = new ChallengeTask();
                task.setChallenge(challenge);
                task.setTaskText(taskRequest.getTaskText());

                if(taskRequest.getTaskType() != null && !taskRequest.getTaskType().isBlank()){
                    task.setTaskType(TaskType.valueOf(taskRequest.getTaskType().toUpperCase()));
                }

                task.setRequiredLabel(taskRequest.getRequiredLabel());

                if (taskRequest.getRequiredCategory() != null && !taskRequest.getRequiredCategory().isBlank()) {
                    task.setRequiredCategory(
                            PictureCategory.valueOf(taskRequest.getRequiredCategory().toUpperCase())
                    );
                }

                task.setRequiredCount(taskRequest.getRequiredCount());
                task.setMustBeUnique(taskRequest.isMustBeUnique());
                task.setReferenceImageUrl(taskRequest.getReferenceImageUrl());
                task.setHelpText(taskRequest.getHelpText());

                challenge.getTasks().add(task);
            }
        }

        Challenge savedChallenge = challengeRepository.save(challenge);

        for(ChallengeTask task : savedChallenge.getTasks()){
            taskResponses.add(toTaskResponse(task));
        }

        return new ChallengeDetailsResponse(
                savedChallenge.getId(),
                savedChallenge.getTitle(),
                savedChallenge.getDescription(),
                savedChallenge.getType().name(),
                savedChallenge.getDifficulty().name(),
                savedChallenge.getRewardPoints(),
                savedChallenge.isActive(),
                savedChallenge.getLocationName(),
                savedChallenge.getStartMonth(),
                savedChallenge.getEndMonth(),
                ChallengeStatus.NOT_STARTED.name(),
                categoryNameOrNull(challenge),
                taskResponses
        );
    }

    @Transactional
    public ChallengeResponse startRandomChallenge(Integer userId, ChallengeStartRandomRequest request) {
        User user = getUserById(userId);

        if (request == null) {
            throw new IllegalArgumentException(EMPTY_RANDOM_CHALLENGE_REQUEST);
        }

        if (request.getChallengeDifficulty() == null || request.getChallengeDifficulty().isBlank()) {
            throw new IllegalArgumentException(EMPTY_RANDOM_CHALLENGE_DIFFICULTY);
        }

        if (request.getChallengeType() == null || request.getChallengeType().isBlank()) {
            throw new IllegalArgumentException(EMPTY_RANDOM_CHALLENGE_TYPE);
        }

        ChallengeDifficulty difficulty = ChallengeDifficulty.valueOf(
                request.getChallengeDifficulty().toUpperCase()
        );

        ChallengeType type = ChallengeType.valueOf(
                request.getChallengeType().toUpperCase()
        );

        PictureCategory category = null;

        if (request.getChallengeCategory() != null && !request.getChallengeCategory().isBlank()) {
            category = PictureCategory.valueOf(
                    request.getChallengeCategory().toUpperCase()
            );
        }

        List<Challenge> matchingChallenges;

        if (category == null) {
            matchingChallenges = challengeRepository.findByActiveTrueAndDifficultyAndType(
                    difficulty,
                    type
            );
        } else {
            matchingChallenges = challengeRepository.findByActiveTrueAndDifficultyAndTypeAndCategory(
                    difficulty,
                    type,
                    category
            );
        }

        List<Challenge> availableChallenges = new ArrayList<>();

        for (Challenge challenge : matchingChallenges) {
            Optional<UserChallengeProgress> existingProgress =
                    userChallengeProgressRepository.findByUserAndChallenge(user, challenge);

            if (existingProgress.isEmpty()) {
                availableChallenges.add(challenge);
            }
        }

        if (availableChallenges.isEmpty()) {
            throw new IllegalArgumentException(NO_RANDOM_CHALLENGE_FOUND);
        }

        Collections.shuffle(availableChallenges);

        Challenge selectedChallenge = availableChallenges.get(0);

        return startChallenge(userId, selectedChallenge.getId());
    }

    private void createTaskProgress(Challenge challenge, UserChallengeProgress savedProgress) {
        for (ChallengeTask task : challenge.getTasks()) {
            UserChallengeTaskProgress taskProgress = new UserChallengeTaskProgress();
            taskProgress.setUserChallengeProgress(savedProgress);
            taskProgress.setChallengeTask(task);
            taskProgress.setCurrentCount(0);
            taskProgress.setCompleted(false);
            taskProgress.setMatchedLabels("");
            userChallengeTaskProgressRepository.save(taskProgress);
        }
    }

    public List<ChallengeResponse> getMyChallenges(Integer userId) {
        User user = getUserById(userId);
        List<UserChallengeProgress> progressList = userChallengeProgressRepository.findByUser(user);

        List<ChallengeResponse> responses = new ArrayList<>();
        for (UserChallengeProgress progress : progressList) {
            responses.add(toChallengeResponse(progress.getChallenge(), progress.getStatus().name()));
        }

        return responses;
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    private String getStatusForUser(User user, Challenge challenge) {
        Optional<UserChallengeProgress> progress = userChallengeProgressRepository.findByUserAndChallenge(user, challenge);

        if (progress.isPresent()) {
            return progress.get().getStatus().name();
        }
        return ChallengeStatus.NOT_STARTED.name();
    }

    private Challenge getChallengeByIdInternal(Integer challengeId) {
        return challengeRepository.findById(challengeId).orElseThrow(() -> new IllegalArgumentException(CHALLENGE_NOT_FOUND));
    }

    private String categoryNameOrNull(Challenge challenge) {
        if (challenge.getCategory() == null) {
            return null;
        }

        return challenge.getCategory().name();
    }

    private ChallengeResponse toChallengeResponse(Challenge challenge, String status) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getType().name(),
                challenge.getDifficulty().name(),
                challenge.getRewardPoints(),
                challenge.isActive(),
                status,
                categoryNameOrNull(challenge)
        );
    }

    private ChallengeTaskResponse toTaskResponse(ChallengeTask challengeTask) {
        String requiredCategory = null;
        if (challengeTask.getRequiredCategory() != null) {
            requiredCategory = challengeTask.getRequiredCategory().name();
        }

        String taskType = null;
        if (challengeTask.getTaskType() != null) {
            taskType = challengeTask.getTaskType().name();
        }

        return new ChallengeTaskResponse(
                challengeTask.getId(),
                challengeTask.getTaskText(),
                taskType,
                challengeTask.getRequiredLabel(),
                requiredCategory,
                challengeTask.getRequiredCount(),
                challengeTask.isMustBeUnique(),
                challengeTask.getReferenceImageUrl(),
                challengeTask.getHelpText()
        );
    }
}
