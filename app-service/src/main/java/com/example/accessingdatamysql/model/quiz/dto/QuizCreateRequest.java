package com.example.accessingdatamysql.model.quiz.dto;

import java.util.List;

public class QuizCreateRequest {

    private String questionText;
    private String difficulty;
    private boolean active;
    private String imageUrl;
    private String explanation;
    private List<QuizOptionCreateRequest> options;

    public QuizCreateRequest() {
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<QuizOptionCreateRequest> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOptionCreateRequest> options) {
        this.options = options;
    }
}