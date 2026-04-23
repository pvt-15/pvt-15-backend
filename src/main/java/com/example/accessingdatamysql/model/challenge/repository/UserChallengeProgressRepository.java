package com.example.accessingdatamysql.model.challenge.repository;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserChallengeProgressRepository extends CrudRepository<UserChallengeProgress, Integer> {

    List<UserChallengeProgress> findByUser(User user);

    Optional<UserChallengeProgress> findByUserAndChallenge(User user, Challenge challenge);

}
