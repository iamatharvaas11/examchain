package com.examchain.generation.fairness;

import com.examchain.core.dto.ApiResponse;
import com.examchain.generation.fairness.FairnessDtos.FairnessEvaluationRequest;
import com.examchain.generation.fairness.FairnessDtos.FairnessReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority/fairness")
public class FairnessController {

    private final FairnessEngineService fairnessService;

    @Autowired
    public FairnessController(FairnessEngineService fairnessService) {
        this.fairnessService = fairnessService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<FairnessReport>> evaluateVariants(
            @RequestBody FairnessEvaluationRequest request
    ) {
        FairnessReport report = fairnessService.evaluateVariants(request.paperIds(), request.customTolerance());
        return ResponseEntity.ok(ApiResponse.success("Fairness evaluation completed", report));
    }
}

