package com.example.accessingdatamysql.quiz.dto;

import java.util.List;

public class QuizSubmitRequest {

    private Integer attemptId;
    private List<AnswerRequest> answers;

    public QuizSubmitRequest() {
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }

    public static class AnswerRequest {
        private Integer questionId;
        private Integer selectedOptionId;

        public AnswerRequest() {
        }

        public Integer getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Integer questionId) {
            this.questionId = questionId;
        }

        public Integer getSelectedOptionId() {
            return selectedOptionId;
        }

        public void setSelectedOptionId(Integer selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }
    }
}