package com.example.accessingdatamysql.model.challenge.service;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.challenge.entity.ChallengeTask;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeTaskProgressRepository;
import com.example.accessingdatamysql.picture.entity.Picture;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeProgressService {

    private final UserChallengeProgressRepository userChallengeProgressRepository;
    private final UserChallengeTaskProgressRepository userChallengeTaskProgressRepository;

    public ChallengeProgressService(UserChallengeProgressRepository userChallengeProgressRepository,
                                    UserChallengeTaskProgressRepository userChallengeTaskProgressRepository){
        this.userChallengeProgressRepository = userChallengeProgressRepository;
        this.userChallengeTaskProgressRepository = userChallengeTaskProgressRepository;
    }

    public int updateProgressFromPicture(User user, Picture picture){
        List<UserChallengeProgress> allProgress = userChallengeProgressRepository.findByUser(user);

        int rewardToGive = 0;

        for(UserChallengeProgress progress : allProgress){
            if(progress.getStatus() != ChallengeStatus.IN_PROGRESS){
                continue;
            }
            List<UserChallengeTaskProgress> taskProgressList =
                    userChallengeTaskProgressRepository.findByUserChallengeProgress(progress);

            boolean anyTaskChanged = false;

            for(UserChallengeTaskProgress taskProgress : taskProgressList){
                if(taskProgress.isCompleted()) {
                    continue;
                }
                ChallengeTask task = taskProgress.getChallengeTask();

                if(!pictureMatchesTask(picture, task)){
                    continue;
                }

                if(task.isMustBeUnique()){
                    String normalizedPictureLabel = normalize(picture.getLabel());
                    if(alreadyMatched(taskProgress, normalizedPictureLabel)){
                        continue;
                    }
                    addMatchedLabel(taskProgress, normalizedPictureLabel);
                }

                int current = taskProgress.getCurrentCount() == null ? 0 : taskProgress.getCurrentCount();
                taskProgress.setCurrentCount(current + 1);

                int requiredCount = task.getRequiredCount() == null ? 1 : task.getRequiredCount();

                if(taskProgress.getCurrentCount() >= requiredCount){
                    taskProgress.setCompleted(true);
                }

                userChallengeTaskProgressRepository.save(taskProgress);
                anyTaskChanged = true;
            }

            if(anyTaskChanged && allTasksCompleted(taskProgressList) && !progress.isRewardClaimed()){
                progress.setStatus(ChallengeStatus.COMPLETED);
                progress.setCompletedAt(LocalDateTime.now());
                progress.setRewardClaimed(true);
                userChallengeProgressRepository.save(progress);

                Integer reward = progress.getChallenge().getRewardPoints();
                if(reward != null){
                    rewardToGive += reward;
                }
            }
        }
        return rewardToGive;
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
}
