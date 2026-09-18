package com.examchain.crypto.service;

import com.examchain.crypto.model.EncryptedPayload;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * High-performance, zero-trust cryptographic service implementing AES-256-GCM
 * authenticated encryption and SHA-256 integrity verification.
 */
@Service
public class CryptoService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12; // 96-bit IV recommended by NIST SP 800-38D
    private static final int GCM_TAG_LENGTH_BITS = 128; // 128-bit authentication tag
    private static final int AES_KEY_SIZE_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    public SecretKey generateAes256Key() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE_BITS, secureRandom);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES algorithm not supported", e);
        }
    }

    public EncryptedPayload encrypt(byte[] plaintext, SecretKey key, byte[] associatedData) {
        if (plaintext == null || key == null) {
            throw new IllegalArgumentException("Plaintext and SecretKey cannot be null");
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            if (associatedData != null && associatedData.length > 0) {
                cipher.updateAAD(associatedData);
            }

            byte[] ciphertext = cipher.doFinal(plaintext);
            String plaintextHash = computeSha256(plaintext);
            String ciphertextHash = computeSha256(ciphertext);

            return new EncryptedPayload(ciphertext, iv, plaintextHash, ciphertextHash);
        } catch (Exception e) {
            throw new SecurityException("AES-256-GCM encryption failed", e);
        }
    }

    public byte[] decrypt(EncryptedPayload payload, SecretKey key, byte[] associatedData) {
        if (payload == null || key == null) {
            throw new IllegalArgumentException("Payload and SecretKey cannot be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload.iv());
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            if (associatedData != null && associatedData.length > 0) {
                cipher.updateAAD(associatedData);
            }

            byte[] decrypted = cipher.doFinal(payload.ciphertext());
            String decryptedHash = computeSha256(decrypted);

            if (payload.sha256PlaintextHash() != null && !payload.sha256PlaintextHash().equalsIgnoreCase(decryptedHash)) {
                throw new SecurityException("Cryptographic integrity violation: Plaintext SHA-256 hash mismatch");
            }

            return decrypted;
        } catch (Exception e) {
            throw new SecurityException("AES-256-GCM decryption failed: Authentication tag mismatch or corrupted payload", e);
        }
    }

    public String computeSha256(byte[] data) {
        if (data == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest unavailable", e);
        }
    }

    public String computeSha256(String text) {
        if (text == null) return "";
        return computeSha256(text.getBytes(StandardCharsets.UTF_8));
    }
}

