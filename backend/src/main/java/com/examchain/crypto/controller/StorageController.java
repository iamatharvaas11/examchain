package com.examchain.crypto.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.crypto.model.StorageReceipt;
import com.examchain.crypto.service.EncryptedStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority/storage")
public class StorageController {

    private final EncryptedStorageService encryptedStorageService;

    @Autowired
    public StorageController(EncryptedStorageService encryptedStorageService) {
        this.encryptedStorageService = encryptedStorageService;
    }

    @GetMapping("/receipt/{paperId}")
    public ResponseEntity<ApiResponse<StorageReceipt>> getReceipt(@PathVariable String paperId) {
        StorageReceipt receipt = encryptedStorageService.getReceipt(paperId);
        return ResponseEntity.ok(ApiResponse.success("Storage receipt retrieved", receipt));
    }

    @GetMapping("/verify/{paperId}")
    public ResponseEntity<ApiResponse<Boolean>> verifyStoragePresence(@PathVariable String paperId) {
        boolean exists = encryptedStorageService.hasEncryptedPaper(paperId);
        return ResponseEntity.ok(ApiResponse.success("Encrypted storage presence verified", exists));
    }
}

