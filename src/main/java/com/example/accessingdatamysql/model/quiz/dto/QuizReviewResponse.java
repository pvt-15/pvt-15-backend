package com.example.accessingdatamysql.model.quiz.dto;

import java.util.List;

public class QuizReviewResponse {

    private Integer attemptId;
    private Integer score;
    private Integer totalQuestions;
    private Integer pointsAwarded;
    private String completedAt;
    private List<QuizAnswerReviewResponse> answers;

    public QuizReviewResponse() {
    }

    public QuizReviewResponse(Integer attemptId,
                              Integer score,
                              Integer totalQuestions,
                              Integer pointsAwarded,
                              String completedAt,
                              List<QuizAnswerReviewResponse> answers) {
        this.attemptId = attemptId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.pointsAwarded = pointsAwarded;
        this.completedAt = completedAt;
        this.answers = answers;
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

    public List<QuizAnswerReviewResponse> getAnswers() {
        return answers;
    }
}