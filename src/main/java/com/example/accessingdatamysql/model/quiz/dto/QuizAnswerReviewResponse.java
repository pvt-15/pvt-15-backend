package com.example.accessingdatamysql.model.quiz.dto;

public class QuizAnswerReviewResponse {

    private Integer questionId;
    private String questionText;
    private String questionImageUrl;

    private Integer selectedOptionId;
    private String selectedOptionText;
    private String selectedOptionImageUrl;

    private Integer correctOptionId;
    private String correctOptionText;
    private String correctOptionImageUrl;

    private boolean correct;
    private String explanation;

    public QuizAnswerReviewResponse() {
    }

    public QuizAnswerReviewResponse(Integer questionId,
                                    String questionText,
                                    String questionImageUrl,
                                    Integer selectedOptionId,
                                    String selectedOptionText,
                                    String selectedOptionImageUrl,
                                    Integer correctOptionId,
                                    String correctOptionText,
                                    String correctOptionImageUrl,
                                    boolean correct,
                                    String explanation) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionImageUrl = questionImageUrl;
        this.selectedOptionId = selectedOptionId;
        this.selectedOptionText = selectedOptionText;
        this.selectedOptionImageUrl = selectedOptionImageUrl;
        this.correctOptionId = correctOptionId;
        this.correctOptionText = correctOptionText;
        this.correctOptionImageUrl = correctOptionImageUrl;
        this.correct = correct;
        this.explanation = explanation;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getQuestionImageUrl() {
        return questionImageUrl;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public String getSelectedOptionText() {
        return selectedOptionText;
    }

    public String getSelectedOptionImageUrl() {
        return selectedOptionImageUrl;
    }

    public Integer getCorrectOptionId() {
        return correctOptionId;
    }

    public String getCorrectOptionText() {
        return correctOptionText;
    }

    public String getCorrectOptionImageUrl() {
        return correctOptionImageUrl;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getExplanation() {
        return explanation;
    }
}