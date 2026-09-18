package com.examchain.crypto.service;

import javax.crypto.SecretKey;

/**
 * Enterprise Key Management Service (KMS) abstraction.
 * Decouples cryptographic operations from physical key storage, allowing seamless
 * transition from HashiCorp Vault transit secrets to AWS KMS or hardware HSMs.
 */
public interface KeyManagementService {

    SecretKey getOrGeneratePaperKey(String paperId);

    void storeKey(String paperId, SecretKey key);

    SecretKey getKey(String paperId);

    boolean hasKey(String paperId);
}

