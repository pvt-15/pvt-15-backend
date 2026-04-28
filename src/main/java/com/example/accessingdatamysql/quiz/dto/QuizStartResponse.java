package com.example.accessingdatamysql.quiz.dto;

import java.util.List;

public class QuizStartResponse {

    private Integer attemptId;
    private String difficulty;
    private List<QuizQuestionResponse> questions;

    public QuizStartResponse() {
    }

    public QuizStartResponse(Integer attemptId,
                             String difficulty,
                             List<QuizQuestionResponse> questions) {
        this.attemptId = attemptId;
        this.difficulty = difficulty;
        this.questions = questions;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<QuizQuestionResponse> getQuestions() {
        return questions;
    }
}