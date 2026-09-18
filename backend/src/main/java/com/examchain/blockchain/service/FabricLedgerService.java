package com.examchain.blockchain.service;

import com.examchain.blockchain.dto.BlockchainDtos.LedgerTransactionDto;
import com.examchain.blockchain.entity.LedgerTransactionEntity;
import com.examchain.blockchain.repository.LedgerTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FabricLedgerService {

    private final FabricGatewayService gatewayService;
    private final LedgerTransactionRepository transactionRepository;

    @Autowired
    public FabricLedgerService(
            FabricGatewayService gatewayService,
            LedgerTransactionRepository transactionRepository
    ) {
        this.gatewayService = gatewayService;
        this.transactionRepository = transactionRepository;
    }

    public LedgerTransactionDto registerQuestionPool(
            String poolId,
            String subjectCode,
            int questionCount,
            String poolHash,
            String setterId
    ) {
        String payload = String.format(
                "{\"poolId\":\"%s\",\"subjectCode\":\"%s\",\"questionCount\":%d,\"poolHash\":\"%s\",\"setterId\":\"%s\"}",
                poolId, subjectCode, questionCount, poolHash, setterId
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("RegisterQuestionPool", poolId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto registerPaper(
            String paperId,
            String examCode,
            String setCode,
            int totalMarks,
            String paperHash,
            String enclaveKeyId
    ) {
        String payload = String.format(
                "{\"paperId\":\"%s\",\"examCode\":\"%s\",\"setCode\":\"%s\",\"totalMarks\":%d,\"paperHash\":\"%s\",\"enclaveKeyId\":\"%s\"}",
                paperId, examCode, setCode, totalMarks, paperHash, enclaveKeyId
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("RegisterPaper", paperId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto recordApproval(
            String paperId,
            String authorityId,
            String authorityRole
    ) {
        String payload = String.format(
                "{\"paperId\":\"%s\",\"authorityId\":\"%s\",\"authorityRole\":\"%s\"}",
                paperId, authorityId, authorityRole
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("RecordApproval", paperId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto authorizeRelease(
            String paperId,
            String authorizedBy
    ) {
        String payload = String.format(
                "{\"paperId\":\"%s\",\"authorizedBy\":\"%s\"}",
                paperId, authorizedBy
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("AuthorizeRelease", paperId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto recordAccess(
            String paperId,
            String centreId,
            String operatorId,
            String action,
            String outcome
    ) {
        String payload = String.format(
                "{\"paperId\":\"%s\",\"centreId\":\"%s\",\"operatorId\":\"%s\",\"action\":\"%s\",\"outcome\":\"%s\"}",
                paperId, centreId, operatorId, action, outcome
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("RecordAccess", paperId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto quarantineVariant(
            String paperId,
            String incidentId,
            String reason,
            String quarantinedBy
    ) {
        String payload = String.format(
                "{\"paperId\":\"%s\",\"incidentId\":\"%s\",\"reason\":\"%s\",\"quarantinedBy\":\"%s\"}",
                paperId, incidentId, reason, quarantinedBy
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("QuarantineVariant", paperId, payload);
        return mapToDto(entity);
    }

    public LedgerTransactionDto replaceVariant(
            String quarantinedPaperId,
            String replacementPaperId,
            String authorizedBy
    ) {
        String payload = String.format(
                "{\"quarantinedPaperId\":\"%s\",\"replacementPaperId\":\"%s\",\"authorizedBy\":\"%s\"}",
                quarantinedPaperId, replacementPaperId, authorizedBy
        );
        LedgerTransactionEntity entity = gatewayService.submitTransaction("ReplaceVariant", quarantinedPaperId, payload);
        return mapToDto(entity);
    }

    public List<LedgerTransactionDto> getTransactionsForEntity(String entityId) {
        return transactionRepository.findByEntityIdOrderByCreatedAtDesc(entityId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<LedgerTransactionDto> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .toList();
    }

    private LedgerTransactionDto mapToDto(LedgerTransactionEntity e) {
        return new LedgerTransactionDto(
                e.getTxId(),
                e.getChannelId(),
                e.getChaincodeId(),
                e.getTransactionName(),
                e.getEntityId(),
                e.getPayloadJson(),
                e.getStatus(),
                e.getBlockNumber(),
                e.getCreatedAt()
        );
    }
}

