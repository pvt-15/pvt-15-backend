package com.example.accessingdatamysql.quiz.repository;

import com.example.accessingdatamysql.quiz.entity.QuizOption;
import com.example.accessingdatamysql.quiz.entity.QuizQuestion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuizOptionRepository extends CrudRepository<QuizOption, Integer> {
    List<QuizOption> findByQuizQuestion(QuizQuestion quizQuestion);
}
