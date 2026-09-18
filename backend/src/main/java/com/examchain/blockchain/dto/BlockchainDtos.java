package com.examchain.blockchain.dto;

import com.examchain.blockchain.model.LedgerTransactionStatus;
import java.time.Instant;
import java.util.List;

public class BlockchainDtos {

    public record LedgerTransactionDto(
            String txId,
            String channelId,
            String chaincodeId,
            String transactionName,
            String entityId,
            String payloadJson,
            LedgerTransactionStatus status,
            Long blockNumber,
            Instant createdAt
    ) {}

    public record OnChainPaperStateDto(
            String paperId,
            String examCode,
            String setCode,
            int totalMarks,
            String paperHash,
            String enclaveKeyId,
            String status,
            int approvalCount,
            boolean isReleased,
            String authorizedBy,
            String releaseAuthorizedAt,
            String quarantinedReason,
            String replacementPaperId,
            List<String> approvals,
            List<String> accessLogs
    ) {}
}
