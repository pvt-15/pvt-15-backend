package com.example.accessingdatamysql.model.quiz.dto;

import com.example.accessingdatamysql.gamification.dto.GamificationUpdateResponse;

/**
 * Response returned after a quiz submission.
 * Contains both the quiz result and gamification updates for the user.
 */
public class QuizSubmitResultResponse {

    private QuizSubmitResponse quiz;
    private GamificationUpdateResponse gamification;

    public QuizSubmitResultResponse(QuizSubmitResponse quiz,
                                    GamificationUpdateResponse gamification) {
        this.quiz = quiz;
        this.gamification = gamification;
    }

    public QuizSubmitResponse getQuiz() {
        return quiz;
    }

    public GamificationUpdateResponse getGamification() {
        return gamification;
    }
}