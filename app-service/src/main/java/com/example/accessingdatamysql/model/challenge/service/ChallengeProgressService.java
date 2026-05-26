package com.example.accessingdatamysql.model.challenge.service;

import com.example.accessingdatamysql.model.challenge.entity.UserChallengePictureMatch;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengePictureMatchRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.model.challenge.entity.ChallengeTask;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeTaskProgressRepository;
import com.example.accessingdatamysql.picture.entity.Picture;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeProgressService {

    private final UserChallengeProgressRepository userChallengeProgressRepository;
    private final UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;
    private final UserChallengePictureMatchRepository userChallengePictureMatchRepository;

    public ChallengeProgressService(UserChallengeProgressRepository userChallengeProgressRepository,
                                    UserChallengeTaskProgressRepository userChallengeTaskProgressRepository,
                                    UserChallengePictureMatchRepository userChallengePictureMatchRepository){
        this.userChallengeProgressRepository = userChallengeProgressRepository;
        this.userChallengeTaskProgressRepository = userChallengeTaskProgressRepository;
        this.userChallengePictureMatchRepository = userChallengePictureMatchRepository;
    }

    @Transactional
    public int updateProgressFromPicture(User user, Picture picture, Integer challengeId) {
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge id is required");
        }

        UserChallengeProgress progress = userChallengeProgressRepository
                .findByUserAndChallenge_Id(user, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge has not been started"));

        if (progress.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Challenge is not in progress");
        }

        List<UserChallengeTaskProgress> taskProgressList =
                userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

        boolean anyTaskChanged = false;

        for (UserChallengeTaskProgress taskProgress : taskProgressList) {
            if (taskProgress.isCompleted()) {
                continue;
            }

            ChallengeTask task = taskProgress.getChallengeTask();

            if (!canPictureBeAddedToTask(picture, taskProgress, task)) {
                continue;
            }

            if (userChallengePictureMatchRepository.existsByTaskProgressAndPicture(taskProgress, picture)) {
                continue;
            }

            if (task.isMustBeUnique()) {
                String uniquenessKey = getUniquenessKey(picture, task);
                addMatchedLabel(taskProgress, uniquenessKey);
            }

            UserChallengePictureMatch pictureMatch = new UserChallengePictureMatch();
            pictureMatch.setTaskProgress(taskProgress);
            pictureMatch.setPicture(picture);
            pictureMatch.setMatchedAt(LocalDateTime.now());
            userChallengePictureMatchRepository.save(pictureMatch);

            int currentCount = taskProgress.getCurrentCount() == null
                    ? 0
                    : taskProgress.getCurrentCount();

            taskProgress.setCurrentCount(currentCount + 1);

            int requiredCount = task.getRequiredCount() == null
                    ? 1
                    : task.getRequiredCount();

            if (taskProgress.getCurrentCount() >= requiredCount) {
                taskProgress.setCompleted(true);
            }

            userChallengeTaskProgressRepository.save(taskProgress);
            anyTaskChanged = true;
        }

        if (anyTaskChanged && allTasksCompleted(taskProgressList) && !progress.isRewardClaimed()) {
            progress.setStatus(ChallengeStatus.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setRewardClaimed(true);
            userChallengeProgressRepository.save(progress);

            Integer reward = progress.getChallenge().getRewardPoints();

            if (reward != null) {
                return reward;
            }
        }

        return 0;
    }

    @Transactional
    public int updateProgressFromDailyPicture(
            User user,
            Picture picture,
            Integer challengeId
    ) {
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge id is required");
        }

        UserChallengeProgress progress = userChallengeProgressRepository
                .findByUserAndChallenge_Id(user, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge has not been started"));

        if (progress.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Challenge is not in progress");
        }

        List<UserChallengeTaskProgress> taskProgressList =
                userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

        UserChallengeTaskProgress taskProgress =
                findPhotoProofTaskProgress(taskProgressList);

        ChallengeTask task = taskProgress.getChallengeTask();

        UserChallengePictureMatch pictureMatch = new UserChallengePictureMatch();
        pictureMatch.setTaskProgress(taskProgress);
        pictureMatch.setPicture(picture);
        pictureMatch.setMatchedAt(LocalDateTime.now());
        userChallengePictureMatchRepository.save(pictureMatch);

        int currentCount = taskProgress.getCurrentCount() == null
                ? 0
                : taskProgress.getCurrentCount();

        taskProgress.setCurrentCount(currentCount + 1);

        int requiredCount = task.getRequiredCount() == null
                ? 1
                : task.getRequiredCount();

        if (taskProgress.getCurrentCount() >= requiredCount) {
            taskProgress.setCompleted(true);
        }

        userChallengeTaskProgressRepository.save(taskProgress);

        if (allTasksCompleted(taskProgressList) && !progress.isRewardClaimed()) {
            progress.setStatus(ChallengeStatus.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setRewardClaimed(true);
            userChallengeProgressRepository.save(progress);

            Integer reward = progress.getChallenge().getRewardPoints();

            if (reward != null) {
                return reward;
            }
        }

        return 0;
    }

    @Transactional
    public boolean matchesAnyTask(User user, Integer challengeId, PictureCategory category, String label) {
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge id is required");
        }

        UserChallengeProgress progress = userChallengeProgressRepository
                .findByUserAndChallenge_Id(user, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge has not been started"));

        if (progress.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Challenge is not in progress");
        }

        List<UserChallengeTaskProgress> taskProgressList =
                userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

        Picture candidate = new Picture();
        candidate.setCategory(category);
        candidate.setLabel(label);

        for (UserChallengeTaskProgress taskProgress : taskProgressList) {
            if (taskProgress.isCompleted()) {
                continue;
            }

            ChallengeTask task = taskProgress.getChallengeTask();

            if (canPictureBeAddedToTask(candidate, taskProgress, task)) {
                return true;
            }
        }

        return false;
    }

    private boolean pictureMatchesTask(Picture picture, ChallengeTask task){
        if(task.getTaskType() == TaskType.CATEGORY){
            return picture.getCategory() == task.getRequiredCategory();
        }
        if(task.getTaskType() == TaskType.LABEL){
            return labelsMatch(picture.getLabel(), task.getRequiredLabel());
        }
        return false;
    }

    private UserChallengeTaskProgress findPhotoProofTaskProgress(
            List<UserChallengeTaskProgress> taskProgressList
    ) {
        UserChallengeTaskProgress foundTaskProgress = null;

        for (UserChallengeTaskProgress taskProgress : taskProgressList) {
            if (taskProgress.isCompleted()) {
                continue;
            }

            ChallengeTask task = taskProgress.getChallengeTask();

            if (task == null || task.getTaskType() != TaskType.PHOTO_PROOF) {
                continue;
            }

            if (foundTaskProgress != null) {
                throw new IllegalArgumentException(
                        "Daily challenge has more than one active photo proof task"
                );
            }

            foundTaskProgress = taskProgress;
        }

        if (foundTaskProgress == null) {
            throw new IllegalArgumentException(
                    "No active photo proof task found for this challenge"
            );
        }

        return foundTaskProgress;
    }

    private boolean labelsMatch(String actualLabel, String requiredLabel){
        String actual = normalize(actualLabel);
        String required = normalize(requiredLabel);

        if(actual.isBlank() || required.isBlank()){
            return false;
        }
        return actual.equals(required) || actual.contains(required) || required.contains(actual);
    }

    private boolean alreadyMatched(UserChallengeTaskProgress taskProgress, String normalizedLabel){
        String matchedLabels = taskProgress.getMatchedLabels();
        if(matchedLabels == null || matchedLabels.isBlank()){
            return false;
        }

        String[] parts = matchedLabels.split(",");
        for(String part : parts){
            if(part.trim().equals(normalizedLabel)){
                return true;
            }
        }
        return false;
    }

    private void addMatchedLabel(UserChallengeTaskProgress taskProgress, String normalizedLabel){
        String matchedLabels = taskProgress.getMatchedLabels();

        if(matchedLabels == null || matchedLabels.isBlank()){
            taskProgress.setMatchedLabels(normalizedLabel);
            return;
        }
        taskProgress.setMatchedLabels(matchedLabels + "," + normalizedLabel);
    }

    private boolean allTasksCompleted(List<UserChallengeTaskProgress> taskProgressList){
        for(UserChallengeTaskProgress taskProgress : taskProgressList){
            if(!taskProgress.isCompleted()){
                return false;
            }
        }
        return true;
    }

    private String normalize(String label) {
        if (label == null) {
            return "";
        }

        String normalized = label.trim().toLowerCase();

        if (normalized.startsWith("plantnet:")) {
            normalized = normalized.substring("plantnet:".length()).trim();
        }
        if (normalized.startsWith("vision:")) {
            normalized = normalized.substring("vision:".length()).trim();
        }

        return normalized;
    }

    private boolean canPictureBeAddedToTask(
            Picture picture,
            UserChallengeTaskProgress taskProgress,
            ChallengeTask task
    ) {
        if (!pictureMatchesTask(picture, task)) {
            return false;
        }

        if (!task.isMustBeUnique()) {
            return true;
        }

        String uniquenessKey = getUniquenessKey(picture, task);

        if (uniquenessKey.isBlank()) {
            return false;
        }

        return !alreadyMatched(taskProgress, uniquenessKey);
    }

    private String getUniquenessKey(Picture picture, ChallengeTask task) {
        if (task.getTaskType() == TaskType.LABEL &&
                task.getRequiredLabel() != null &&
                !task.getRequiredLabel().isBlank()) {
            return normalize(task.getRequiredLabel());
        }

        return normalize(picture.getLabel());
    }
}
