package com.examchain.audit.controller;

import com.examchain.audit.dto.AuditorDtos.*;
import com.examchain.audit.service.AuditorDashboardService;
import com.examchain.core.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditorDashboardController {

    private final AuditorDashboardService auditorService;

    @Autowired
    public AuditorDashboardController(AuditorDashboardService auditorService) {
        this.auditorService = auditorService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AuditorDashboardSummary>> getSummary() {
        AuditorDashboardSummary summary = auditorService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Auditor dashboard summary retrieved", summary));
    }

    @GetMapping("/papers/{paperId}/graph")
    public ResponseEntity<ApiResponse<ProvenanceGraphResponse>> getProvenanceGraph(@PathVariable String paperId) {
        ProvenanceGraphResponse graph = auditorService.getProvenanceGraph(paperId);
        return ResponseEntity.ok(ApiResponse.success("Paper provenance graph retrieved", graph));
    }

    @PostMapping("/papers/{paperId}/verify")
    public ResponseEntity<ApiResponse<CryptographicVerificationResult>> verifyPaperCryptographicIntegrity(
            @PathVariable String paperId
    ) {
        CryptographicVerificationResult result = auditorService.verifyPaperCryptographicIntegrity(paperId);
        return ResponseEntity.ok(ApiResponse.success("Cryptographic integrity verification completed", result));
    }
}

