package com.example.accessingdatamysql.model.quiz.entity;

import jakarta.persistence.*;

@Entity
public class UserQuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_quiz_attempt_id", nullable = false)
    private UserQuizAttempt userQuizAttempt;

    @ManyToOne
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion;

    @ManyToOne
    @JoinColumn(name = "selected_option_id", nullable = false)
    private QuizOption selectedOption;

    private boolean correct;

    public UserQuizAnswer() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserQuizAttempt getUserQuizAttempt() {
        return userQuizAttempt;
    }

    public void setUserQuizAttempt(UserQuizAttempt userQuizAttempt) {
        this.userQuizAttempt = userQuizAttempt;
    }

    public QuizQuestion getQuizQuestion() {
        return quizQuestion;
    }

    public void setQuizQuestion(QuizQuestion quizQuestion) {
        this.quizQuestion = quizQuestion;
    }

    public QuizOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(QuizOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}