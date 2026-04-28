package com.example.accessingdatamysql.quiz.repository;

import com.example.accessingdatamysql.quiz.entity.UserQuizAnswer;
import com.example.accessingdatamysql.quiz.entity.UserQuizAttempt;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserQuizAnswerRepository extends CrudRepository<UserQuizAnswer, Integer> {
    List<UserQuizAnswer> findByUserQuizAttempt(UserQuizAttempt userQuizAttempt);
}