package com.examchain.crypto.model;

import java.time.Instant;
import java.util.HexFormat;

public record EncryptedPayload(
        byte[] ciphertext,
        byte[] iv,
        String sha256PlaintextHash,
        String sha256CiphertextHash
) {
    public String getIvHex() {
        return HexFormat.of().formatHex(iv);
    }

    public String getCiphertextHex() {
        return HexFormat.of().formatHex(ciphertext);
    }
}

