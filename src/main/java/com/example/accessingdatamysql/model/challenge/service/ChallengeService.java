package com.example.accessingdatamysql.model.challenge.service;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeDetailsResponse;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeResponse;
import com.example.accessingdatamysql.model.challenge.dto.ChallengeTaskResponse;
import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import com.example.accessingdatamysql.model.challenge.entity.ChallengeTask;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import com.example.accessingdatamysql.model.challenge.repository.ChallengeRepository;
import com.example.accessingdatamysql.model.challenge.repository.UserChallengeProgressRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String CHALLENGE_NOT_FOUND = "Challenge not found";

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeProgressRepository userChallengeProgressRepository;

    public ChallengeService(ChallengeRepository challengeRepository,
                            UserRepository userRepository,
                            UserChallengeProgressRepository userChallengeProgressRepository){
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.userChallengeProgressRepository = userChallengeProgressRepository;
    }

    public List<ChallengeResponse> getActiveChallenges(Integer userId){
        User user = getUserById(userId);
        List<Challenge> challenges = challengeRepository.findByActiveTrue();

        List<ChallengeResponse> responses = new ArrayList<>();
        for(Challenge challenge : challenges){
            String status = getStatusForUser(user, challenge);
            responses.add(toChallengeResponse(challenge, status));
        }
        return responses;
    }

    public ChallengeDetailsResponse getChallengeById(Integer userId, Integer challengeId){
        User user = getUserById(userId);
        Challenge challenge = getChallengeByIdInternal(challengeId);

        String status = getStatusForUser(user, challenge);

        List<ChallengeTaskResponse> taskResponses = new ArrayList<>();
        for(ChallengeTask task : challenge.getTasks()){
            taskResponses.add(toTaskResponse(task));
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    private String getStatusForUser(User user, Challenge challenge) {
        Optional<UserChallengeProgress> progress = userChallengeProgressRepository.findByUserAndChallenge(user, challenge);

        if(progress.isPresent()){
            return progress.get().getStatus().name();
        }
        return ChallengeStatus.NOT_STARTED.name();
    }

    private Challenge getChallengeByIdInternal(Integer challengeId){
        return challengeRepository.findById(challengeId).orElseThrow(() -> new IllegalArgumentException(CHALLENGE_NOT_FOUND));
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
                status
        );
    }

    private ChallengeTaskResponse toTaskResponse(ChallengeTask challengeTask){
        String requiredCategory = null;
        if(challengeTask.getRequiredCategory() != null){
            requiredCategory = challengeTask.getRequiredCategory().name();
        }

        String taskType = null;
        if(challengeTask.getTaskType() != null){
            taskType = challengeTask.getTaskType().name();
        }

        return new ChallengeTaskResponse(
                challengeTask.getId(),
                challengeTask.getTaskText(),
                taskType,
                challengeTask.getRequiredLabel(),
                requiredCategory,
                challengeTask.getRequiredCount(),
                challengeTask.isMustBeUnique()
        );
    }
}
