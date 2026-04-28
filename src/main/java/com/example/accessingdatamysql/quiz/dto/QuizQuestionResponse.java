package com.example.accessingdatamysql.quiz.dto;

import java.util.List;

public class QuizQuestionResponse {

    private Integer id;
    private String questionText;
    private String imageUrl;
    private List<QuizOptionResponse> options;

    public QuizQuestionResponse() {
    }

    public QuizQuestionResponse(Integer id,
                                String questionText,
                                String imageUrl,
                                List<QuizOptionResponse> options) {
        this.id = id;
        this.questionText = questionText;
        this.imageUrl = imageUrl;
        this.options = options;
    }

    public Integer getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<QuizOptionResponse> getOptions() {
        return options;
    }
}