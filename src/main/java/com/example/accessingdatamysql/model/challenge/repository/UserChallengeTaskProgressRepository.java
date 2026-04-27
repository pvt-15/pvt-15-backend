package com.example.accessingdatamysql.model.challenge.repository;

import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserChallengeTaskProgressRepository extends CrudRepository<UserChallengeTaskProgress, Integer> {

    List<UserChallengeTaskProgress> findByUserChallengeProgress(UserChallengeProgress progress);
}
