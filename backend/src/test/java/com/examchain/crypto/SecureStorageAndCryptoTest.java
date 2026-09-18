package com.examchain.crypto;

import com.examchain.ExamchainApplication;
import com.examchain.crypto.model.EncryptedPayload;
import com.examchain.crypto.model.StorageReceipt;
import com.examchain.crypto.service.CryptoService;
import com.examchain.crypto.service.EncryptedStorageService;
import com.examchain.storage.service.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamchainApplication.class)
@ActiveProfiles("test")
public class SecureStorageAndCryptoTest {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private EncryptedStorageService encryptedStorageService;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("AES-256-GCM Roundtrip: Encrypt and decrypt recovers exact original plaintext")
    void testAesGcmEncryptionDecryptionRoundtrip() {
        String originalContent = "CONFIDENTIAL_EXAM_PAPER_2026: Quantum Computing & Shor's Algorithm";
        byte[] plaintext = originalContent.getBytes(StandardCharsets.UTF_8);
        byte[] aad = "PAPER-QC-001:EXAM-2026-PHYS".getBytes(StandardCharsets.UTF_8);

        SecretKey key = cryptoService.generateAes256Key();
        EncryptedPayload payload = cryptoService.encrypt(plaintext, key, aad);

        assertThat(payload).isNotNull();
        assertThat(payload.ciphertext()).isNotEqualTo(plaintext);
        assertThat(payload.iv()).hasSize(12);
        assertThat(payload.sha256PlaintextHash()).isEqualTo(cryptoService.computeSha256(plaintext));

        byte[] decrypted = cryptoService.decrypt(payload, key, aad);
        assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo(originalContent);
    }

    @Test
    @DisplayName("Tamper Detection: Modified ciphertext fails authentication tag verification")
    void testTamperedCiphertextRejection() {
        byte[] plaintext = "CONFIDENTIAL_PAPER_DATA".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "PAPER-01:EXAM-01".getBytes(StandardCharsets.UTF_8);
        SecretKey key = cryptoService.generateAes256Key();

        EncryptedPayload payload = cryptoService.encrypt(plaintext, key, aad);

        // Tamper with one byte in the ciphertext
        byte[] tamperedCiphertext = payload.ciphertext().clone();
        tamperedCiphertext[0] ^= 0xFF; // flip bits

        EncryptedPayload tamperedPayload = new EncryptedPayload(
                tamperedCiphertext,
                payload.iv(),
                payload.sha256PlaintextHash(),
                cryptoService.computeSha256(tamperedCiphertext)
        );

        assertThatThrownBy(() -> cryptoService.decrypt(tamperedPayload, key, aad))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Authentication tag mismatch or corrupted payload");
    }

    @Test
    @DisplayName("Tamper Detection: Tampered AAD (exam code / paper ID mismatch) fails authentication")
    void testTamperedAadRejection() {
        byte[] plaintext = "CONFIDENTIAL_PAPER_DATA".getBytes(StandardCharsets.UTF_8);
        byte[] originalAad = "PAPER-01:EXAM-CS101".getBytes(StandardCharsets.UTF_8);
        byte[] forgedAad = "PAPER-01:EXAM-CS999".getBytes(StandardCharsets.UTF_8);

        SecretKey key = cryptoService.generateAes256Key();
        EncryptedPayload payload = cryptoService.encrypt(plaintext, key, originalAad);

        assertThatThrownBy(() -> cryptoService.decrypt(payload, key, forgedAad))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Authentication tag mismatch or corrupted payload");
    }

    @Test
    @DisplayName("Wrong-Key Rejection: Decrypting with wrong key fails cryptographic authentication")
    void testWrongKeyRejection() {
        byte[] plaintext = "CONFIDENTIAL_EXAM_DATA".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "PAPER-XYZ:EXAM-XYZ".getBytes(StandardCharsets.UTF_8);

        SecretKey correctKey = cryptoService.generateAes256Key();
        SecretKey attackerKey = cryptoService.generateAes256Key();

        EncryptedPayload payload = cryptoService.encrypt(plaintext, correctKey, aad);

        assertThatThrownBy(() -> cryptoService.decrypt(payload, attackerKey, aad))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Authentication tag mismatch or corrupted payload");
    }

    @Test
    @DisplayName("Zero-Trust Storage: Plaintext is NEVER stored in object storage")
    void testZeroTrustObjectStorageWorkflow() {
        String paperId = "PAPER-CRYPTO-2026";
        String examCode = "EXAM-CYBER-SEC";
        String secretPlaintext = "TOP_SECRET_EXAM_QUESTION: Factorize N = p*q with 2048 bits";
        byte[] plaintextBytes = secretPlaintext.getBytes(StandardCharsets.UTF_8);

        // Store encrypted paper
        StorageReceipt receipt = encryptedStorageService.storeEncryptedPaper(paperId, plaintextBytes, examCode);

        assertThat(receipt).isNotNull();
        assertThat(receipt.paperId()).isEqualTo(paperId);
        assertThat(receipt.objectKey()).isEqualTo("papers/" + paperId + ".enc");

        // Inspect raw object storage bytes
        byte[] storedBytes = objectStorageService.getObject(receipt.bucket(), receipt.objectKey());
        String storedString = new String(storedBytes, StandardCharsets.UTF_8);

        // Plaintext must NEVER appear in raw storage
        assertThat(storedString).doesNotContain("TOP_SECRET_EXAM_QUESTION");
        assertThat(storedString).doesNotContain("Factorize");

        // Decrypt paper through authorized service
        byte[] retrieved = encryptedStorageService.retrieveDecryptedPaper(paperId, examCode);
        assertThat(new String(retrieved, StandardCharsets.UTF_8)).isEqualTo(secretPlaintext);
    }

    @Test
    @DisplayName("Storage RBAC: STUDENT cannot access storage receipts; EXAM_AUTHORITY can")
    void testStorageRbac() throws Exception {
        String paperId = "PAPER-RBAC-TEST";
        encryptedStorageService.storeEncryptedPaper(paperId, "Mock paper content".getBytes(StandardCharsets.UTF_8), "EXAM-TEST");

        // 1. STUDENT receives 403 Forbidden
        mockMvc.perform(get("/api/v1/authority/storage/receipt/" + paperId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());

        // 2. EXAM_AUTHORITY receives 200 OK
        mockMvc.perform(get("/api/v1/authority/storage/receipt/" + paperId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paperId").value(paperId));
    }
}

