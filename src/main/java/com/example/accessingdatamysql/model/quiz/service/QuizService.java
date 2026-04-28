package com.example.accessingdatamysql.model.quiz.service;

import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.model.quiz.dto.*;
import com.example.accessingdatamysql.model.quiz.quiz.dto.*;
import com.example.accessingdatamysql.quiz.dto.*;
import com.example.accessingdatamysql.model.quiz.entity.QuizOption;
import com.example.accessingdatamysql.model.quiz.entity.QuizQuestion;
import com.example.accessingdatamysql.model.quiz.entity.UserQuizAnswer;
import com.example.accessingdatamysql.model.quiz.entity.UserQuizAttempt;
import com.example.accessingdatamysql.model.quiz.enums.QuizDifficulty;
import com.example.accessingdatamysql.model.quiz.repository.QuizOptionRepository;
import com.example.accessingdatamysql.model.quiz.repository.QuizQuestionRepository;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAnswerRepository;
import com.example.accessingdatamysql.model.quiz.repository.UserQuizAttemptRepository;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuizService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String QUIZ_ATTEMPT_NOT_FOUND = "Quiz attempt not found";
    private static final String QUIZ_QUESTION_MOT_FOUND = "Quiz question not found";
    private static final String QUIZ_OPTION_NOT_FOUND = "Quiz option not found";

    private static final double RATIO_FOR_FULL_POINTS = 0.8;
    private static final double RATIO_FOR_HALF_POINTS = 0.5;
    private static final int POINTS_AWARDED_EASY = 20;
    private static final int POINTS_AWARDED_MEDIUM = 30;
    private static final int POINTS_AWARDED_HARD = 50;

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final UserRepository userRepository;

    public QuizService(QuizQuestionRepository quizQuestionRepository,
                       QuizOptionRepository quizOptionRepository,
                       UserQuizAttemptRepository userQuizAttemptRepository,
                       UserQuizAnswerRepository userQuizAnswerRepository,
                       UserRepository userRepository) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.userQuizAnswerRepository = userQuizAnswerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuizStartResponse startQuiz(Integer userId, String difficulty, Integer count) {

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        QuizDifficulty quizDifficulty = QuizDifficulty.valueOf(difficulty.toUpperCase());

        List<QuizQuestion> questions = quizQuestionRepository.findByActiveTrueAndDifficulty(quizDifficulty);

        Collections.shuffle(questions);

        int numberOfQuestions = count == null || count <= 0 ? 5 : count;

        if (questions.size() > numberOfQuestions) {
            questions = questions.subList(0, numberOfQuestions);
        }

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUser(user);
        attempt.setDifficulty(quizDifficulty);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setTotalQuestions(questions.size());
        attempt.setPointsAwarded(0);

        UserQuizAttempt savedAttempt = userQuizAttemptRepository.save(attempt);

        List<QuizQuestionResponse> questionResponses = new ArrayList<>();
        for (QuizQuestion question : questions) {
            List<QuizOptionResponse> optionResponses = new ArrayList<>();

            List<QuizOption> options = quizOptionRepository.findByQuizQuestion(question);
            for (QuizOption option : options) {
                optionResponses.add(new QuizOptionResponse(
                        option.getId(),
                        option.getOptionText(),
                        option.getImageUrl()
                ));
            }
            questionResponses.add(new QuizQuestionResponse(
                    question.getId(),
                    question.getQuestionText(),
                    question.getImageUrl(),
                    optionResponses
            ));
        }
        return new QuizStartResponse(
                savedAttempt.getId(),
                savedAttempt.getDifficulty().name(),
                questionResponses
        );
    }

    @Transactional
    public QuizSubmitResponse submitQuiz(Integer userId, QuizSubmitRequest request){

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        UserQuizAttempt attempt = userQuizAttemptRepository.findById(request.getAttemptId()).orElseThrow(() -> new IllegalArgumentException(QUIZ_ATTEMPT_NOT_FOUND));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(QUIZ_ATTEMPT_NOT_FOUND);
        }

        int score = 0;

        for(QuizSubmitRequest.AnswerRequest answerRequest : request.getAnswers()){
            QuizQuestion question = quizQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException(QUIZ_QUESTION_MOT_FOUND));

            QuizOption selectedOption = quizOptionRepository.findById(answerRequest.getSelectedOptionId())
                    .orElseThrow(() -> new IllegalArgumentException(QUIZ_OPTION_NOT_FOUND));

            boolean correct = selectedOption.isCorrect();
            if(correct){
                score++;
            }

            UserQuizAnswer userQuizAnswer = new UserQuizAnswer();
            userQuizAnswer.setUserQuizAttempt(attempt);
            userQuizAnswer.setQuizQuestion(question);
            userQuizAnswer.setSelectedOption(selectedOption);
            userQuizAnswer.setCorrect(correct);

            userQuizAnswerRepository.save(userQuizAnswer);
        }

        int pointsAwarded = calculateQuizPoints(attempt.getDifficulty(), score, attempt.getTotalQuestions());

        attempt.setScore(score);
        attempt.setPointsAwarded(pointsAwarded);
        attempt.setCompletedAt(LocalDateTime.now());
        userQuizAttemptRepository.save(attempt);

        user.setTotalPoints(user.getTotalPoints() + pointsAwarded);
        user.setLevel(calculateLevel(user.getTotalPoints()));
        userRepository.save(user);

        return new QuizSubmitResponse(
                attempt.getId(),
                score,
                attempt.getTotalQuestions(),
                pointsAwarded,
                attempt.getCompletedAt().toString()
        );
    }

    private int calculateQuizPoints(QuizDifficulty difficulty, int score, int totalQuestions) {
        if (totalQuestions == 0) {
            return 0;
        }

        double ratio = (double) score / totalQuestions;

        if (difficulty == QuizDifficulty.EASY) {
            if (ratio >= RATIO_FOR_FULL_POINTS) return POINTS_AWARDED_EASY;
            if (ratio >= RATIO_FOR_HALF_POINTS) return POINTS_AWARDED_EASY / 2;
            return 0;
        }

        if (difficulty == QuizDifficulty.MEDIUM) {
            if (ratio >= RATIO_FOR_FULL_POINTS) return POINTS_AWARDED_MEDIUM;
            if (ratio >= RATIO_FOR_HALF_POINTS) return POINTS_AWARDED_MEDIUM / 2;
            return 0;
        }

        if (ratio >= RATIO_FOR_FULL_POINTS) return POINTS_AWARDED_HARD;
        if (ratio >= RATIO_FOR_HALF_POINTS) return POINTS_AWARDED_HARD / 2;
        return 0;
    }

    private Level calculateLevel(int totalPoints){
        if (totalPoints >= 300) {
            return Level.LEVEL_3;
        }
        if (totalPoints >= 150) {
            return Level.LEVEL_2;
        }
        return Level.LEVEL_1;
    }
}
