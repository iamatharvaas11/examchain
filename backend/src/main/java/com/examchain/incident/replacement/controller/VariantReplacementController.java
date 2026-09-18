package com.examchain.incident.replacement.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.incident.replacement.dto.ReplacementDtos.*;
import com.examchain.incident.replacement.service.VariantReplacementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authority/replacement")
public class VariantReplacementController {

    private final VariantReplacementService replacementService;

    @Autowired
    public VariantReplacementController(VariantReplacementService replacementService) {
        this.replacementService = replacementService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ReplacementResponse>> executeReplacement(
            @Valid @RequestBody ExecuteReplacementRequest request
    ) {
        ReplacementResponse response = replacementService.executeReplacement(request);
        return ResponseEntity.ok(ApiResponse.success("Variant replacement executed successfully", response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ReplacementResponse>>> getHistory() {
        List<ReplacementResponse> history = replacementService.getReplacementHistory();
        return ResponseEntity.ok(ApiResponse.success("Variant replacement history retrieved", history));
    }
}

