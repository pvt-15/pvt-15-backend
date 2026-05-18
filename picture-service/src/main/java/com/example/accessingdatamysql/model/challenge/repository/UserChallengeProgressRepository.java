package com.example.accessingdatamysql.model.challenge.repository;

import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeType;
import com.example.accessingdatamysql.user.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserChallengeProgressRepository extends CrudRepository<UserChallengeProgress, Integer> {

    List<UserChallengeProgress> findByUser(User user);

    Optional<UserChallengeProgress> findByUserAndChallenge(User user, Challenge challenge);

    Optional<UserChallengeProgress> findByUserAndChallenge_Id(User user, Integer challengeId);

    boolean existsByUserAndChallenge_TypeAndStartedAtBetween(
            User user,
            ChallengeType type,
            LocalDateTime start,
            LocalDateTime end
    );
}