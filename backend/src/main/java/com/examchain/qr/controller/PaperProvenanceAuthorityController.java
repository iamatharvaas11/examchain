package com.examchain.qr.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.qr.dto.ProvenanceDtos.CreateTraceRequest;
import com.examchain.qr.dto.ProvenanceDtos.CreateTraceResponse;
import com.examchain.qr.service.PaperProvenanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority/provenance")
public class PaperProvenanceAuthorityController {

    private final PaperProvenanceService provenanceService;

    @Autowired
    public PaperProvenanceAuthorityController(PaperProvenanceService provenanceService) {
        this.provenanceService = provenanceService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<CreateTraceResponse>> registerTrace(
            @Valid @RequestBody CreateTraceRequest request
    ) {
        CreateTraceResponse response = provenanceService.registerPaperTrace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paper trace generated successfully", response));
    }
}
