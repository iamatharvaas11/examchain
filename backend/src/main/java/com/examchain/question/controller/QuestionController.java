package com.examchain.question.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.question.dto.QuestionDtos.QuestionRequest;
import com.examchain.question.dto.QuestionDtos.QuestionResponse;
import com.examchain.question.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // --- Setter Endpoints ---

    @PostMapping("/setter/pools/{poolId}/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @PathVariable UUID poolId,
            @Valid @RequestBody QuestionRequest request,
            Authentication authentication
    ) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        QuestionResponse response = questionService.createQuestion(poolId, request, setterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Question created successfully", response));
    }

    @GetMapping("/setter/pools/{poolId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByPoolId(@PathVariable UUID poolId) {
        List<QuestionResponse> response = questionService.getQuestionsByPoolId(poolId);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved", response));
    }

    @GetMapping("/setter/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable UUID questionId) {
        QuestionResponse response = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(ApiResponse.success("Question retrieved", response));
    }

    @PutMapping("/setter/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionRequest request,
            Authentication authentication
    ) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        QuestionResponse response = questionService.updateQuestion(questionId, request, setterId);
        return ResponseEntity.ok(ApiResponse.success("Question updated successfully", response));
    }

    @DeleteMapping("/setter/questions/{questionId}")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(
            @PathVariable UUID questionId,
            Authentication authentication
    ) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        questionService.deleteQuestion(questionId, setterId);
        return ResponseEntity.ok(ApiResponse.success("Question deleted successfully", "Deleted: " + questionId));
    }

    // --- Authority Endpoints ---

    @GetMapping("/authority/pools/{poolId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAuthorityQuestionsByPoolId(@PathVariable UUID poolId) {
        List<QuestionResponse> response = questionService.getQuestionsByPoolId(poolId);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved for review", response));
    }
}
