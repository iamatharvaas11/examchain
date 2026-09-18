package com.examchain.audit;

import com.examchain.audit.dto.AuditorDtos.*;
import com.examchain.audit.service.AuditorDashboardService;
import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.centre.repository.PrintAuditLogRepository;
import com.examchain.crypto.service.CryptoService;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.dto.BlueprintDtos.PaperQuestionSummary;
import com.examchain.generation.repository.GeneratedPaperRepository;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.repository.SecurityIncidentRepository;
import com.examchain.incident.service.FreezeModeService;
import com.examchain.qr.entity.PaperProvenanceEntity;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;
import com.examchain.question.repository.QuestionPoolRepository;
import com.examchain.release.repository.PaperApprovalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuditorDashboardTest {

    private QuestionPoolRepository poolRepository;
    private GeneratedPaperRepository paperRepository;
    private PaperApprovalRepository approvalRepository;
    private PrintAuditLogRepository printAuditLogRepository;
    private SecurityIncidentRepository incidentRepository;
    private FreezeModeService freezeModeService;
    private FabricLedgerService ledgerService;
    private DynamicPaperGenerationService generationService;
    private PaperProvenanceRepository provenanceRepository;
    private CryptoService cryptoService;

    private AuditorDashboardService auditorService;

    @BeforeEach
    void setUp() {
        poolRepository = Mockito.mock(QuestionPoolRepository.class);
        paperRepository = Mockito.mock(GeneratedPaperRepository.class);
        approvalRepository = Mockito.mock(PaperApprovalRepository.class);
        printAuditLogRepository = Mockito.mock(PrintAuditLogRepository.class);
        incidentRepository = Mockito.mock(SecurityIncidentRepository.class);
        freezeModeService = Mockito.mock(FreezeModeService.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        provenanceRepository = Mockito.mock(PaperProvenanceRepository.class);
        cryptoService = Mockito.mock(CryptoService.class);

        auditorService = new AuditorDashboardService(
                poolRepository,
                paperRepository,
                approvalRepository,
                printAuditLogRepository,
                incidentRepository,
                freezeModeService,
                ledgerService,
                generationService,
                provenanceRepository,
                cryptoService
        );
    }

    @Test
    @DisplayName("Dashboard summary accurately aggregates operational KPIs")
    void testGetDashboardSummary() {
        when(poolRepository.count()).thenReturn(5L);
        when(paperRepository.count()).thenReturn(12L);
        when(approvalRepository.count()).thenReturn(24L);
        when(printAuditLogRepository.count()).thenReturn(150L);
        when(incidentRepository.count()).thenReturn(1L);
        when(freezeModeService.getCurrentLevel()).thenReturn(FreezeLevel.NORMAL);
        when(incidentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        AuditorDashboardSummary summary = auditorService.getDashboardSummary();

        assertThat(summary.totalQuestionPools()).isEqualTo(5L);
        assertThat(summary.totalGeneratedPapers()).isEqualTo(12L);
        assertThat(summary.totalApprovalsRecorded()).isEqualTo(24L);
        assertThat(summary.totalPrintOperations()).isEqualTo(150L);
        assertThat(summary.totalAnomaliesReported()).isEqualTo(1L);
        assertThat(summary.activeFreezeLevel()).isEqualTo(FreezeLevel.NORMAL);
    }

    @Test
    @DisplayName("Provenance graph DAG connects all lifecycle stages from Pool to Public Receipt")
    void testGetProvenanceGraph() {
        String paperId = "PAP-AUDIT-001";
        UUID subjectId = UUID.randomUUID();
        UUID bpId = UUID.randomUUID();

        GeneratedPaperResponse paper = new GeneratedPaperResponse(
                UUID.randomUUID(), paperId, bpId, subjectId, "SET_A", 100, 10, "valid-hash", "RELEASED", Collections.emptyList(), Instant.now()
        );
        when(generationService.getPaperByPaperId(paperId)).thenReturn(paper);
        when(approvalRepository.findByPaperId(paperId)).thenReturn(Collections.emptyList());

        PaperProvenanceEntity prov = new PaperProvenanceEntity("TRC-AUDIT-999", paperId, "valid-hash", "CS101", "Networks", "C1", Instant.now(), Instant.now());
        when(provenanceRepository.findByPaperId(paperId)).thenReturn(Optional.of(prov));

        ProvenanceGraphResponse graph = auditorService.getProvenanceGraph(paperId);

        assertThat(graph.paperId()).isEqualTo(paperId);
        assertThat(graph.nodes()).isNotEmpty();
        assertThat(graph.edges()).isNotEmpty();

        List<String> nodeTypes = graph.nodes().stream().map(ProvenanceGraphNode::type).toList();
        assertThat(nodeTypes).contains("POOL", "BLUEPRINT", "PAPER", "CRYPTO_ENCLAVE", "APPROVAL", "RECEIPT");
    }

    @Test
    @DisplayName("Cryptographic integrity verification passes when computed hash matches ledger")
    void testCryptographicVerification_Authentic() {
        String paperId = "PAP-AUTHENTIC";
        String paperHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        List<PaperQuestionSummary> questions = List.of(
                new PaperQuestionSummary(1, "Q1", 1, 5, DifficultyLevel.EASY, QuestionType.MCQ, "Sample text", "hash-q1")
        );
        GeneratedPaperResponse paper = new GeneratedPaperResponse(
                UUID.randomUUID(), paperId, UUID.randomUUID(), UUID.randomUUID(), "SET_A", 5, 1, paperHash, "RELEASED", questions, Instant.now()
        );
        when(generationService.getPaperByPaperId(paperId)).thenReturn(paper);
        when(ledgerService.getTransactionsForEntity(paperId)).thenReturn(Collections.emptyList());

        CryptographicVerificationResult result = auditorService.verifyPaperCryptographicIntegrity(paperId);

        assertThat(result.hashMatch()).isTrue();
        assertThat(result.poolHashVerified()).isTrue();
        assertThat(result.tamperDetected()).isFalse();
        assertThat(result.status()).isEqualTo("VERIFIED_AUTHENTIC");
    }

    @Test
    @DisplayName("Tamper detected when questions have invalid/missing hash")
    void testCryptographicVerification_TamperDetected() {
        String paperId = "PAP-CORRUPTED";
        String paperHash = "some-hash";

        List<PaperQuestionSummary> questions = List.of(
                new PaperQuestionSummary(1, "Q1", 1, 5, DifficultyLevel.EASY, QuestionType.MCQ, "Sample text", "") // Empty question hash!
        );
        GeneratedPaperResponse paper = new GeneratedPaperResponse(
                UUID.randomUUID(), paperId, UUID.randomUUID(), UUID.randomUUID(), "SET_A", 5, 1, paperHash, "RELEASED", questions, Instant.now()
        );
        when(generationService.getPaperByPaperId(paperId)).thenReturn(paper);

        CryptographicVerificationResult result = auditorService.verifyPaperCryptographicIntegrity(paperId);

        assertThat(result.tamperDetected()).isTrue();
        assertThat(result.status()).isEqualTo("TAMPER_DETECTED");
    }
}

