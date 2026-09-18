package com.examchain.incident.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.incident.dto.IncidentDtos.*;
import com.examchain.incident.service.FreezeModeService;
import com.examchain.incident.service.IncidentResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authority/incidents")
public class IncidentResponseController {

    private final FreezeModeService freezeModeService;
    private final IncidentResponseService incidentService;

    @Autowired
    public IncidentResponseController(
            FreezeModeService freezeModeService,
            IncidentResponseService incidentService
    ) {
        this.freezeModeService = freezeModeService;
        this.incidentService = incidentService;
    }

    @GetMapping("/freeze-status")
    public ResponseEntity<ApiResponse<FreezeStateResponse>> getFreezeStatus() {
        FreezeStateResponse response = freezeModeService.getFreezeState();
        return ResponseEntity.ok(ApiResponse.success("Current freeze status retrieved", response));
    }

    @PostMapping("/freeze")
    public ResponseEntity<ApiResponse<FreezeStateResponse>> updateFreeze(
            @Valid @RequestBody UpdateFreezeRequest request
    ) {
        FreezeStateResponse response = freezeModeService.updateFreezeLevel(
                request.freezeLevel(), request.reason(), request.triggeredBy()
        );
        return ResponseEntity.ok(ApiResponse.success("Freeze level updated successfully", response));
    }

    @PostMapping("/quarantine")
    public ResponseEntity<ApiResponse<SecurityIncidentDto>> quarantineVariant(
            @Valid @RequestBody QuarantineVariantRequest request
    ) {
        SecurityIncidentDto response = incidentService.quarantineVariant(request);
        return ResponseEntity.ok(ApiResponse.success("Paper variant quarantined", response));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<SecurityIncidentDto>>> getIncidentEvents() {
        List<SecurityIncidentDto> events = incidentService.getAllIncidents();
        return ResponseEntity.ok(ApiResponse.success("Incident events retrieved", events));
    }
}

