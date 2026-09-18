package com.examchain.offline.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.offline.dto.OfflineDtos.*;
import com.examchain.offline.service.OfflineSyncService;
import com.examchain.offline.service.OfflineTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offline")
public class OfflineRecoveryController {

    private final OfflineTokenService tokenService;
    private final OfflineSyncService syncService;

    @Autowired
    public OfflineRecoveryController(OfflineTokenService tokenService, OfflineSyncService syncService) {
        this.tokenService = tokenService;
        this.syncService = syncService;
    }

    @PostMapping("/tokens/generate")
    public ResponseEntity<ApiResponse<GeneratedTokenResponse>> generateToken(
            @Valid @RequestBody GenerateOfflineTokenRequest request
    ) {
        GeneratedTokenResponse response = tokenService.generateOfflineToken(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency offline token generated", response));
    }

    @PostMapping("/tokens/verify-consume")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> consumeToken(
            @Valid @RequestBody ConsumeTokenRequest request
    ) {
        TokenValidationResponse response = tokenService.validateAndConsumeToken(request);
        return ResponseEntity.ok(ApiResponse.success("Offline token validated and consumed", response));
    }

    @PostMapping("/queue")
    public ResponseEntity<ApiResponse<OfflineAuditItemDto>> queueAuditEvent(
            @Valid @RequestBody QueueAuditEventRequest request
    ) {
        OfflineAuditItemDto response = syncService.queueAuditEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offline audit event queued locally", response));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncReconciliationResponse>> syncWithLedger(
            @RequestParam String centreCode
    ) {
        SyncReconciliationResponse response = syncService.synchronizeWithLedger(centreCode);
        return ResponseEntity.ok(ApiResponse.success("Offline events reconciled with ledger", response));
    }

    @GetMapping("/queue/{centreCode}")
    public ResponseEntity<ApiResponse<List<OfflineAuditItemDto>>> getQueue(
            @PathVariable String centreCode
    ) {
        List<OfflineAuditItemDto> queue = syncService.getQueueStatus(centreCode);
        return ResponseEntity.ok(ApiResponse.success("Offline queue retrieved", queue));
    }
}
