package com.examchain.centre.service;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.centre.dto.CentreDtos.*;
import com.examchain.centre.entity.CentreTerminalEntity;
import com.examchain.centre.entity.PrintAuditLogEntity;
import com.examchain.centre.repository.CentreTerminalRepository;
import com.examchain.centre.repository.PrintAuditLogRepository;
import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class SecurePrintingService {

    private static final Logger log = LoggerFactory.getLogger(SecurePrintingService.class);

    private final CentreTerminalRepository terminalRepository;
    private final PrintAuditLogRepository printAuditLogRepository;
    private final ReleaseScheduleRepository releaseScheduleRepository;
    private final DeviceFingerprintService fingerprintService;
    private final DynamicPaperGenerationService generationService;
    private final FabricLedgerService ledgerService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public SecurePrintingService(
            CentreTerminalRepository terminalRepository,
            PrintAuditLogRepository printAuditLogRepository,
            ReleaseScheduleRepository releaseScheduleRepository,
            DeviceFingerprintService fingerprintService,
            DynamicPaperGenerationService generationService,
            FabricLedgerService ledgerService
    ) {
        this.terminalRepository = terminalRepository;
        this.printAuditLogRepository = printAuditLogRepository;
        this.releaseScheduleRepository = releaseScheduleRepository;
        this.fingerprintService = fingerprintService;
        this.generationService = generationService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public TerminalResponse registerTerminal(RegisterTerminalRequest request) {
        CentreTerminalEntity terminal = terminalRepository.findByCentreCodeAndTerminalId(request.centreCode(), request.terminalId())
                .orElseGet(() -> new CentreTerminalEntity(request.centreCode(), request.terminalId(), request.deviceFingerprint()));

        terminal.setRegisteredFingerprint(request.deviceFingerprint());
        terminal.setLastSeenAt(Instant.now());
        terminal.setStatus("TRUSTED");

        CentreTerminalEntity saved = terminalRepository.save(terminal);
        log.info("Registered terminal [{}] for centre [{}]", saved.getTerminalId(), saved.getCentreCode());

        return new TerminalResponse(saved.getCentreCode(), saved.getTerminalId(), saved.getStatus(), saved.getLastSeenAt());
    }

    @Transactional
    public SecurePrintResponse executeSecurePrint(String paperId, SecurePrintRequest request) {
        // 1. Release Status & Time-lock check
        ReleaseScheduleEntity schedule = releaseScheduleRepository.findByPaperId(paperId)
                .orElseThrow(() -> new ResourceNotFoundException("No release schedule found for paper: " + paperId));

        if (schedule.getStatus() != ReleaseStatus.RELEASED) {
            throw new IllegalStateException("Centre cannot access paper before official time-lock release (current status: " + schedule.getStatus() + ")");
        }

        // 2. Evaluate Device Continuity & Risk Score
        int riskScore = fingerprintService.evaluateRiskScore(request.centreCode(), request.terminalId(), request.deviceFingerprint());
        if (riskScore >= 90) {
            throw new IllegalStateException("Print access blocked: Terminal risk score " + riskScore + " exceeds security threshold.");
        }

        // 3. In-memory paper retrieval & decryption (Zero plaintext persisted to disk)
        GeneratedPaperResponse paper = generationService.getPaperByPaperId(paperId);

        // 4. Generate forensic watermark
        Instant printTimestamp = Instant.now();
        String watermark = String.format(
                "CONFIDENTIAL — CENTRE: %s | TERMINAL: %s | OP: %s | TIME: %s | COPY: %d",
                request.centreCode(), request.terminalId(), request.operatorId(), printTimestamp, request.copyCount()
        );

        List<WatermarkedQuestionDto> watermarkedQuestions = paper.questions().stream()
                .map(q -> new WatermarkedQuestionDto(
                        q.sequenceNumber(),
                        q.questionText(),
                        q.marks()
                ))
                .toList();

        // 5. Generate unique print receipt ID
        byte[] nonce = new byte[8];
        secureRandom.nextBytes(nonce);
        String receiptId = "PRN-REC-" + HexFormat.of().formatHex(nonce).toUpperCase();

        // 6. Record in local PostgreSQL audit log
        PrintAuditLogEntity auditLog = new PrintAuditLogEntity(
                paperId,
                request.centreCode(),
                request.terminalId(),
                request.operatorId(),
                request.deviceFingerprint(),
                riskScore,
                watermark,
                request.copyCount(),
                receiptId,
                "SUCCESS"
        );
        printAuditLogRepository.save(auditLog);

        // 7. Record immutable access log on Hyperledger Fabric ledger
        ledgerService.recordAccess(paperId, request.centreCode(), request.operatorId(), "SECURE_PRINT", "SUCCESS");
        log.info("Completed secure print for paper [{}] at centre [{}] under receipt [{}]",
                paperId, request.centreCode(), receiptId);

        return new SecurePrintResponse(
                receiptId,
                paperId,
                request.centreCode(),
                request.terminalId(),
                request.operatorId(),
                riskScore,
                watermark,
                request.copyCount(),
                "SUCCESS",
                printTimestamp,
                watermarkedQuestions
        );
    }

    @Transactional(readOnly = true)
    public PrintReceiptDto getReceipt(String receiptId) {
        PrintAuditLogEntity logEntity = printAuditLogRepository.findByReceiptId(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Print receipt not found: " + receiptId));

        return new PrintReceiptDto(
                logEntity.getReceiptId(),
                logEntity.getPaperId(),
                logEntity.getCentreCode(),
                logEntity.getTerminalId(),
                logEntity.getOperatorId(),
                logEntity.getRiskScore(),
                logEntity.getWatermarkText(),
                logEntity.getCopyCount(),
                logEntity.getStatus(),
                logEntity.getCreatedAt()
        );
    }
}

