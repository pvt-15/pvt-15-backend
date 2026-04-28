package com.example.accessingdatamysql.model.quiz.dto;

public class QuizSubmitResponse {

    private Integer attemptId;
    private Integer score;
    private Integer totalQuestions;
    private Integer pointsAwarded;
    private String completedAt;

    public QuizSubmitResponse() {
    }

    public QuizSubmitResponse(Integer attemptId,
                              Integer score,
                              Integer totalQuestions,
                              Integer pointsAwarded,
                              String completedAt) {
        this.attemptId = attemptId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.pointsAwarded = pointsAwarded;
        this.completedAt = completedAt;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}