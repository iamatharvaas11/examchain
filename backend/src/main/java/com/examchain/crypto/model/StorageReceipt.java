package com.examchain.crypto.model;

import java.time.Instant;

public record StorageReceipt(
        String paperId,
        String bucket,
        String objectKey,
        String sha256PlaintextHash,
        String sha256CiphertextHash,
        String ivHex,
        long ciphertextSize,
        Instant timestamp
) {}

