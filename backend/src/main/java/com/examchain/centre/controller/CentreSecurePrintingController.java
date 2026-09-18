package com.examchain.centre.controller;

import com.examchain.centre.dto.CentreDtos.*;
import com.examchain.centre.service.SecurePrintingService;
import com.examchain.core.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/centre")
public class CentreSecurePrintingController {

    private final SecurePrintingService printingService;

    @Autowired
    public CentreSecurePrintingController(SecurePrintingService printingService) {
        this.printingService = printingService;
    }

    @PostMapping("/terminals")
    public ResponseEntity<ApiResponse<TerminalResponse>> registerTerminal(
            @Valid @RequestBody RegisterTerminalRequest request
    ) {
        TerminalResponse response = printingService.registerTerminal(request);
        return ResponseEntity.ok(ApiResponse.success("Terminal registered successfully", response));
    }

    @PostMapping("/papers/{paperId}/print")
    public ResponseEntity<ApiResponse<SecurePrintResponse>> executeSecurePrint(
            @PathVariable String paperId,
            @Valid @RequestBody SecurePrintRequest request
    ) {
        SecurePrintResponse response = printingService.executeSecurePrint(paperId, request);
        return ResponseEntity.ok(ApiResponse.success("Watermarked print payload generated successfully", response));
    }

    @GetMapping("/receipts/{receiptId}")
    public ResponseEntity<ApiResponse<PrintReceiptDto>> getPrintReceipt(
            @PathVariable String receiptId
    ) {
        PrintReceiptDto response = printingService.getReceipt(receiptId);
        return ResponseEntity.ok(ApiResponse.success("Print receipt retrieved", response));
    }
}

