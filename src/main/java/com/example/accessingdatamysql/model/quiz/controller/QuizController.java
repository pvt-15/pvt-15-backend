package com.example.accessingdatamysql.model.quiz.controller;

import com.example.accessingdatamysql.model.quiz.dto.QuizStartResponse;
import com.example.accessingdatamysql.model.quiz.dto.QuizSubmitRequest;
import com.example.accessingdatamysql.model.quiz.dto.QuizSubmitResponse;
import com.example.accessingdatamysql.model.quiz.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<QuizStartResponse> startQuiz(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam String difficulty,
                                                       @RequestParam(defaultValue = "5") Integer count) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(quizService.startQuiz(userId, difficulty, count));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponse> submitQuiz(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestBody QuizSubmitRequest request) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.ok(quizService.submitQuiz(userId, request));
    }
}
