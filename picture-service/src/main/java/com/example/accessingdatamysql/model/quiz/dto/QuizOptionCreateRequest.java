package com.example.accessingdatamysql.model.quiz.dto;

public class QuizOptionCreateRequest {

    private String optionText;
    private String imageUrl;
    private boolean correct;

    public QuizOptionCreateRequest() {
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}