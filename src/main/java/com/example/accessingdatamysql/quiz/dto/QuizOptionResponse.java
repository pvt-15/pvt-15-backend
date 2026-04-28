package com.example.accessingdatamysql.quiz.dto;

public class QuizOptionResponse {

    private Integer id;
    private String optionText;
    private String imageUrl;

    public QuizOptionResponse() {
    }

    public QuizOptionResponse(Integer id, String optionText, String imageUrl) {
        this.id = id;
        this.optionText = optionText;
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}