package com.examchain.crypto.service;

import com.examchain.crypto.model.EncryptedPayload;
import com.examchain.crypto.model.StorageReceipt;
import com.examchain.storage.service.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * End-to-end Encrypted Storage Service.
 * Enforces Zero-Trust rule: Plaintext examination papers NEVER touch persistent object storage.
 */
@Service
public class EncryptedStorageService {

    private static final Logger log = LoggerFactory.getLogger(EncryptedStorageService.class);
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private final CryptoService cryptoService;
    private final KeyManagementService keyManagementService;
    private final ObjectStorageService objectStorageService;
    private final String bucketName;

    private final Map<String, StorageReceipt> receipts = new ConcurrentHashMap<>();

    @Autowired
    public EncryptedStorageService(
            CryptoService cryptoService,
            KeyManagementService keyManagementService,
            ObjectStorageService objectStorageService,
            @Value("${examchain.minio.bucket:examchain-papers}") String bucketName
    ) {
        this.cryptoService = cryptoService;
        this.keyManagementService = keyManagementService;
        this.objectStorageService = objectStorageService;
        this.bucketName = bucketName;
    }

    public StorageReceipt storeEncryptedPaper(String paperId, byte[] plaintextPaper, String examCode) {
        if (paperId == null || plaintextPaper == null || plaintextPaper.length == 0) {
            throw new IllegalArgumentException("Paper ID and non-empty plaintext are required");
        }

        log.info("Encrypting and storing paper [{}] for exam [{}]", paperId, examCode);

        // 1. Retrieve or generate paper key via KMS
        SecretKey key = keyManagementService.getOrGeneratePaperKey(paperId);

        // 2. Encrypt with AES-256-GCM using paperId and examCode as Authenticated Associated Data (AAD)
        byte[] aad = (paperId + ":" + examCode).getBytes(StandardCharsets.UTF_8);
        EncryptedPayload payload = cryptoService.encrypt(plaintextPaper, key, aad);

        // 3. Assemble storage binary: [12 bytes IV] + [ciphertext + 16 bytes GCM auth tag]
        byte[] packaged = new byte[GCM_IV_LENGTH_BYTES + payload.ciphertext().length];
        System.arraycopy(payload.iv(), 0, packaged, 0, GCM_IV_LENGTH_BYTES);
        System.arraycopy(payload.ciphertext(), 0, packaged, GCM_IV_LENGTH_BYTES, payload.ciphertext().length);

        // 4. Store ONLY ciphertext binary in MinIO
        String objectKey = "papers/" + paperId + ".enc";
        objectStorageService.putObject(bucketName, objectKey, packaged, "application/octet-stream");

        // 5. Generate and register immutable storage receipt
        StorageReceipt receipt = new StorageReceipt(
                paperId,
                bucketName,
                objectKey,
                payload.sha256PlaintextHash(),
                payload.sha256CiphertextHash(),
                payload.getIvHex(),
                packaged.length,
                Instant.now()
        );

        receipts.put(paperId, receipt);
        return receipt;
    }

    public byte[] retrieveDecryptedPaper(String paperId, String examCode) {
        String objectKey = "papers/" + paperId + ".enc";
        byte[] packaged = objectStorageService.getObject(bucketName, objectKey);

        if (packaged.length < GCM_IV_LENGTH_BYTES + 16) {
            throw new SecurityException("Malformed encrypted artifact in storage: insufficient bytes for IV and tag");
        }

        // 1. Extract IV and ciphertext
        byte[] iv = Arrays.copyOfRange(packaged, 0, GCM_IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(packaged, GCM_IV_LENGTH_BYTES, packaged.length);

        // 2. Fetch KMS key
        SecretKey key = keyManagementService.getKey(paperId);

        // 3. Verify AAD and decrypt
        byte[] aad = (paperId + ":" + examCode).getBytes(StandardCharsets.UTF_8);
        StorageReceipt receipt = receipts.get(paperId);
        String expectedPlaintextHash = receipt != null ? receipt.sha256PlaintextHash() : null;

        EncryptedPayload payload = new EncryptedPayload(ciphertext, iv, expectedPlaintextHash, null);
        return cryptoService.decrypt(payload, key, aad);
    }

    public StorageReceipt getReceipt(String paperId) {
        StorageReceipt receipt = receipts.get(paperId);
        if (receipt == null) {
            throw new IllegalArgumentException("No storage receipt found for paper [" + paperId + "]");
        }
        return receipt;
    }

    public boolean hasEncryptedPaper(String paperId) {
        String objectKey = "papers/" + paperId + ".enc";
        return objectStorageService.exists(bucketName, objectKey);
    }
}

