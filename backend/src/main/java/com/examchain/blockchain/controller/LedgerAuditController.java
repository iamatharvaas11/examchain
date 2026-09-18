package com.examchain.blockchain.controller;

import com.examchain.blockchain.dto.BlockchainDtos.LedgerTransactionDto;
import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.core.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit/ledger")
public class LedgerAuditController {

    private final FabricLedgerService ledgerService;

    @Autowired
    public LedgerAuditController(FabricLedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<LedgerTransactionDto>>> getAllTransactions() {
        List<LedgerTransactionDto> transactions = ledgerService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("All on-chain audit transactions retrieved", transactions));
    }

    @GetMapping("/entities/{entityId}/transactions")
    public ResponseEntity<ApiResponse<List<LedgerTransactionDto>>> getTransactionsForEntity(
            @PathVariable String entityId
    ) {
        List<LedgerTransactionDto> transactions = ledgerService.getTransactionsForEntity(entityId);
        return ResponseEntity.ok(ApiResponse.success("Ledger transactions retrieved for entity " + entityId, transactions));
    }
}

