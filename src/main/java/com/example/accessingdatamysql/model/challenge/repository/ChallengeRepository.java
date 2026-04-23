package com.example.accessingdatamysql.model.challenge.repository;

import com.example.accessingdatamysql.model.challenge.entity.Challenge;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChallengeRepository extends CrudRepository<Challenge, Integer> {

    List<Challenge> findByActiveTrue();
}
