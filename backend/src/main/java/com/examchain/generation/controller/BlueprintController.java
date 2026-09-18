package com.examchain.generation.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.generation.dto.BlueprintDtos.*;
import com.examchain.generation.service.DynamicPaperGenerationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authority")
public class BlueprintController {

    private final DynamicPaperGenerationService generationService;

    @Autowired
    public BlueprintController(DynamicPaperGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/subjects/{subjectId}/blueprints")
    public ResponseEntity<ApiResponse<BlueprintResponse>> createBlueprint(
            @PathVariable UUID subjectId,
            @Valid @RequestBody BlueprintRequest request
    ) {
        BlueprintResponse response = generationService.createBlueprint(subjectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Blueprint created successfully", response));
    }

    @GetMapping("/subjects/{subjectId}/blueprints")
    public ResponseEntity<ApiResponse<List<BlueprintResponse>>> getBlueprintsForSubject(@PathVariable UUID subjectId) {
        List<BlueprintResponse> response = generationService.getBlueprintsForSubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success("Blueprints retrieved", response));
    }

    @GetMapping("/blueprints/{blueprintId}")
    public ResponseEntity<ApiResponse<BlueprintResponse>> getBlueprintById(@PathVariable UUID blueprintId) {
        BlueprintResponse response = generationService.getBlueprintById(blueprintId);
        return ResponseEntity.ok(ApiResponse.success("Blueprint retrieved", response));
    }

    @PostMapping("/blueprints/{blueprintId}/generate")
    public ResponseEntity<ApiResponse<GeneratedPaperResponse>> generatePaper(
            @PathVariable UUID blueprintId,
            @Valid @RequestBody GeneratePaperRequest request
    ) {
        GeneratedPaperResponse response = generationService.generatePaper(blueprintId, request.setCode(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Paper generated and encrypted successfully", response));
    }

    @GetMapping("/papers/{paperId}")
    public ResponseEntity<ApiResponse<GeneratedPaperResponse>> getPaperByPaperId(@PathVariable String paperId) {
        GeneratedPaperResponse response = generationService.getPaperByPaperId(paperId);
        return ResponseEntity.ok(ApiResponse.success("Paper details retrieved", response));
    }
}

