package com.example.accessingdatamysql.quiz.repository;

import com.example.accessingdatamysql.quiz.entity.QuizQuestion;
import com.example.accessingdatamysql.quiz.enums.QuizDifficulty;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuizQuestionRepository extends CrudRepository<QuizQuestion, Integer> {
    List<QuizQuestion> findByActiveTrueAndDifficulty(QuizDifficulty difficulty);
}
