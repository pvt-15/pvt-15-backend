package com.example.accessingdatamysql.model.challenge.seed;

import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import com.example.accessingdatamysql.model.challenge.entity.ChallengeTask;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeDifficulty;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeType;
import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.model.challenge.repository.ChallengeRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeeder implements CommandLineRunner {

    private final ChallengeRepository challengeRepository;

    public ChallengeSeeder(ChallengeRepository challengeRepository){
        this.challengeRepository = challengeRepository;
    }

    @Override
    public void run(String... args){
        if(challengeRepository.count() > 0){
            return;
        }

        seedEasyFlowerHunt();
        seedMediumForestHunt();
        seedHardTreeBingo();
        seedHumlegardenLocationChallenge();
    }

    private void seedEasyFlowerHunt() {
        Challenge challenge = createChallenge(
                "Blomjakten",
                "Hitta enkla saker under promenaden.",
                ChallengeType.TREASURE_HUNT,
                ChallengeDifficulty.EASY,
                100,
                true,
                3,
                9,
                null
        );

        addCategoryTask(challenge, "Hitta 1 blomma", PictureCategory.FLOWER, 1, false);
        addCategoryTask(challenge, "Hitta 1 träd", PictureCategory.TREE, 1, false);

        challengeRepository.save(challenge);
    }

    private void seedMediumForestHunt() {
        Challenge challenge = createChallenge(
                "Skogsletaren",
                "Hitta lite mer specifika saker i naturen.",
                ChallengeType.TREASURE_HUNT,
                ChallengeDifficulty.MEDIUM,
                150,
                true,
                3,
                10,
                null
        );

        addLabelTask(challenge, "Hitta en maskros", "dandelion", 1, false);
        addLabelTask(challenge, "Hitta mossa", "moss", 1, false);

        challengeRepository.save(challenge);
    }

    private void seedHardTreeBingo() {
        Challenge challenge = createChallenge(
                "Trädbingo",
                "Hitta fyra olika sorters träd.",
                ChallengeType.BINGO,
                ChallengeDifficulty.HARD,
                200,
                true,
                1,
                12,
                null
        );

        addLabelTask(challenge, "Hitta en gran", "spruce", 1, true);
        addLabelTask(challenge, "Hitta en tall", "pine", 1, true);
        addLabelTask(challenge, "Hitta en björk", "birch", 1, true);
        addLabelTask(challenge, "Hitta en ek", "oak", 1, true);

        challengeRepository.save(challenge);
    }

    private void seedHumlegardenLocationChallenge() {
        Challenge challenge = createChallenge(
                "Humlegården",
                "Hitta färgglada blommor och en humla i Humlegården.",
                ChallengeType.LOCATION,
                ChallengeDifficulty.MEDIUM,
                175,
                true,
                4,
                9,
                "Humlegården"
        );

        addCategoryTask(challenge, "Hitta 2 olika blommor", PictureCategory.FLOWER, 2, true);
        addLabelTask(challenge, "Hitta en humla", "bumblebee", 1, false);

        challengeRepository.save(challenge);
    }

    private Challenge createChallenge(String title,
                                      String description,
                                      ChallengeType type,
                                      ChallengeDifficulty difficulty,
                                      int rewardPoints,
                                      boolean active,
                                      Integer startMonth,
                                      Integer endMonth,
                                      String locationName) {
        Challenge challenge = new Challenge();
        challenge.setTitle(title);
        challenge.setDescription(description);
        challenge.setType(type);
        challenge.setDifficulty(difficulty);
        challenge.setRewardPoints(rewardPoints);
        challenge.setActive(active);
        challenge.setStartMonth(startMonth);
        challenge.setEndMonth(endMonth);
        challenge.setLocationName(locationName);
        return challenge;
    }

    private void addCategoryTask(Challenge challenge,
                                 String taskText,
                                 PictureCategory requiredCategory,
                                 int requiredCount,
                                 boolean mustBeUnique) {
        ChallengeTask task = new ChallengeTask();
        task.setChallenge(challenge);
        task.setTaskText(taskText);
        task.setTaskType(TaskType.CATEGORY);
        task.setRequiredCategory(requiredCategory);
        task.setRequiredCount(requiredCount);
        task.setMustBeUnique(mustBeUnique);

        challenge.getTasks().add(task);
    }

    private void addLabelTask(Challenge challenge,
                              String taskText,
                              String requiredLabel,
                              int requiredCount,
                              boolean mustBeUnique) {
        ChallengeTask task = new ChallengeTask();
        task.setChallenge(challenge);
        task.setTaskText(taskText);
        task.setTaskType(TaskType.LABEL);
        task.setRequiredLabel(requiredLabel);
        task.setRequiredCount(requiredCount);
        task.setMustBeUnique(mustBeUnique);

        challenge.getTasks().add(task);
    }
}
