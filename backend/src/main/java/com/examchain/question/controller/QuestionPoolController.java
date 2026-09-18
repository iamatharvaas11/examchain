package com.examchain.question.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.question.dto.QuestionDtos.*;
import com.examchain.question.service.QuestionPoolService;
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
public class QuestionPoolController {

    private final QuestionPoolService poolService;

    @Autowired
    public QuestionPoolController(QuestionPoolService poolService) {
        this.poolService = poolService;
    }

    // --- Setter Endpoints ---

    @PostMapping("/setter/pools")
    public ResponseEntity<ApiResponse<QuestionPoolResponse>> createPool(
            @Valid @RequestBody QuestionPoolRequest request,
            Authentication authentication
    ) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        QuestionPoolResponse response = poolService.createPool(request, setterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Question pool created", response));
    }

    @GetMapping("/setter/pools")
    public ResponseEntity<ApiResponse<List<QuestionPoolResponse>>> getMyPools(Authentication authentication) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        List<QuestionPoolResponse> response = poolService.getPoolsByCreator(setterId);
        return ResponseEntity.ok(ApiResponse.success("Question pools retrieved", response));
    }

    @GetMapping("/setter/pools/{poolId}")
    public ResponseEntity<ApiResponse<QuestionPoolResponse>> getSetterPoolById(@PathVariable UUID poolId) {
        QuestionPoolResponse response = poolService.getPoolById(poolId);
        return ResponseEntity.ok(ApiResponse.success("Question pool retrieved", response));
    }

    @PostMapping("/setter/pools/{poolId}/submit")
    public ResponseEntity<ApiResponse<QuestionPoolResponse>> submitPool(
            @PathVariable UUID poolId,
            Authentication authentication
    ) {
        String setterId = authentication != null ? authentication.getName() : "setter";
        QuestionPoolResponse response = poolService.submitPool(poolId, setterId);
        return ResponseEntity.ok(ApiResponse.success("Question pool submitted for approval", response));
    }

    // --- Authority Endpoints ---

    @GetMapping("/authority/pools")
    public ResponseEntity<ApiResponse<List<QuestionPoolResponse>>> getAllPools() {
        List<QuestionPoolResponse> response = poolService.getAllPools();
        return ResponseEntity.ok(ApiResponse.success("All question pools retrieved", response));
    }

    @GetMapping("/authority/pools/{poolId}")
    public ResponseEntity<ApiResponse<QuestionPoolResponse>> getAuthorityPoolById(@PathVariable UUID poolId) {
        QuestionPoolResponse response = poolService.getPoolById(poolId);
        return ResponseEntity.ok(ApiResponse.success("Question pool retrieved", response));
    }

    @PostMapping("/authority/pools/{poolId}/review")
    public ResponseEntity<ApiResponse<QuestionPoolResponse>> reviewPool(
            @PathVariable UUID poolId,
            @Valid @RequestBody PoolApprovalRequest request,
            Authentication authentication
    ) {
        String authorityId = authentication != null ? authentication.getName() : "authority";
        QuestionPoolResponse response = poolService.reviewPool(poolId, request.status(), authorityId);
        return ResponseEntity.ok(ApiResponse.success("Question pool review completed", response));
    }
}
