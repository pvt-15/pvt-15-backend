package com.example.accessingdatamysql.model.quiz.repository;

import com.example.accessingdatamysql.model.quiz.entity.QuizQuestion;
import com.example.accessingdatamysql.model.quiz.enums.QuizDifficulty;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuizQuestionRepository extends CrudRepository<QuizQuestion, Integer> {
    List<QuizQuestion> findByActiveTrueAndDifficulty(QuizDifficulty difficulty);
}
