package com.examchain.release.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.release.dto.ReleaseDtos.*;
import com.examchain.release.service.ThresholdApprovalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority/release")
public class ThresholdApprovalController {

    private final ThresholdApprovalService approvalService;

    @Autowired
    public ThresholdApprovalController(ThresholdApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/schedules")
    public ResponseEntity<ApiResponse<ReleaseStatusResponse>> scheduleRelease(
            @Valid @RequestBody ScheduleReleaseRequest request
    ) {
        ReleaseStatusResponse response = approvalService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Release schedule created successfully", response));
    }

    @PostMapping("/papers/{paperId}/approvals")
    public ResponseEntity<ApiResponse<ReleaseStatusResponse>> submitApproval(
            @PathVariable String paperId,
            @Valid @RequestBody SubmitApprovalRequest request
    ) {
        ReleaseStatusResponse response = approvalService.submitApproval(paperId, request);
        return ResponseEntity.ok(ApiResponse.success("Authority approval recorded", response));
    }

    @PostMapping("/papers/{paperId}/authorize")
    public ResponseEntity<ApiResponse<ReleaseStatusResponse>> authorizeRelease(
            @PathVariable String paperId,
            @Valid @RequestBody AuthorizeReleaseRequest request
    ) {
        ReleaseStatusResponse response = approvalService.authorizeRelease(paperId, request.authorizedBy());
        return ResponseEntity.ok(ApiResponse.success("Paper release authorized successfully", response));
    }

    @GetMapping("/papers/{paperId}/status")
    public ResponseEntity<ApiResponse<ReleaseStatusResponse>> getStatus(
            @PathVariable String paperId
    ) {
        ReleaseStatusResponse response = approvalService.getReleaseStatus(paperId);
        return ResponseEntity.ok(ApiResponse.success("Release status retrieved", response));
    }
}

