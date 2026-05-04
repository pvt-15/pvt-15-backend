package com.example.accessingdatamysql.model.quiz.controller;

import com.example.accessingdatamysql.model.quiz.dto.*;
import com.example.accessingdatamysql.model.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private static final String ADMIN_KEY_NOT_CONFIGURED = "Quiz admin key is not configured";

    private final QuizService quizService;
    private final String adminKey;

    public QuizController(QuizService quizService,
                          @Value("${challenge.admin.key:}") String adminKey) {
        this.quizService = quizService;
        this.adminKey = adminKey;
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

    @PostMapping("/admin")
    public ResponseEntity<QuizQuestionResponse> createQuiz(
            @RequestHeader("ADMIN-KEY") String providedAdminKey,
            @RequestBody QuizCreateRequest request) {

        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalStateException(ADMIN_KEY_NOT_CONFIGURED);
        }

        if (!adminKey.equals(providedAdminKey)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(quizService.createQuiz(request));
    }
}
