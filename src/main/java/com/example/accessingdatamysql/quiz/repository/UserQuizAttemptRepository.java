package com.example.accessingdatamysql.quiz.repository;

import com.example.accessingdatamysql.quiz.entity.UserQuizAttempt;
import com.example.accessingdatamysql.user.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserQuizAttemptRepository extends CrudRepository<UserQuizAttempt, Integer> {
    List<UserQuizAttempt> findByUser(User user);
}