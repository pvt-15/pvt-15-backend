package com.example.accessingdatamysql.model.challenge.repository;

import com.example.accessingdatamysql.model.challenge.entity.UserChallengePictureMatch;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeProgress;
import com.example.accessingdatamysql.model.challenge.entity.UserChallengeTaskProgress;
import com.example.accessingdatamysql.picture.entity.Picture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserChallengePictureMatchRepository extends CrudRepository<UserChallengePictureMatch, Integer> {

    boolean existsByTaskProgressAndPicture(UserChallengeTaskProgress taskProgress, Picture picture);

    List<UserChallengePictureMatch> findByTaskProgress_UserChallengeProgress(UserChallengeProgress progress);

    List<UserChallengePictureMatch> findByPicture(Picture picture);
}