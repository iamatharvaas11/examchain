package com.examchain.qr.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.qr.dto.ProvenanceDtos.VerificationResponse;
import com.examchain.qr.service.PaperProvenanceService;
import com.examchain.qr.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verify")
public class PaperProvenancePublicController {

    private final PaperProvenanceService provenanceService;
    private final RateLimiterService rateLimiterService;

    @Autowired
    public PaperProvenancePublicController(
            PaperProvenanceService provenanceService,
            RateLimiterService rateLimiterService
    ) {
        this.provenanceService = provenanceService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/paper/{traceId}")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyPaper(
            @PathVariable String traceId,
            HttpServletRequest request
    ) {
        String clientIp = request.getRemoteAddr() != null ? request.getRemoteAddr() : "anonymous";
        if (!rateLimiterService.tryAcquire(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Rate limit exceeded. Please wait before retrying verification requests."));
        }

        VerificationResponse response = provenanceService.verifyPaper(traceId);
        return ResponseEntity.ok(ApiResponse.success("Verification query processed", response));
    }
}

