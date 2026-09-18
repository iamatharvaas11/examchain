package com.examchain.audit.dto;

import com.examchain.incident.dto.IncidentDtos.SecurityIncidentDto;
import com.examchain.incident.model.FreezeLevel;
import java.time.Instant;
import java.util.List;

public class AuditorDtos {

    public record AuditTimelineEvent(
            String stage,
            String entityId,
            String actor,
            Instant timestamp,
            String status,
            String txId,
            String details
    ) {}

    public record ProvenanceGraphNode(
            String id,
            String label,
            String type, // POOL, BLUEPRINT, PAPER, CRYPTO_ENCLAVE, APPROVAL, PRINT, RECEIPT
            String status,
            Instant timestamp
    ) {}

    public record ProvenanceGraphEdge(
            String from,
            String to,
            String label
    ) {}

    public record ProvenanceGraphResponse(
            String paperId,
            List<ProvenanceGraphNode> nodes,
            List<ProvenanceGraphEdge> edges,
            Instant generatedAt
    ) {}

    public record CryptographicVerificationResult(
            String paperId,
            String computedPaperHash,
            String onChainPaperHash,
            boolean hashMatch,
            boolean poolHashVerified,
            boolean tamperDetected,
            String status,
            Instant verifiedAt
    ) {}

    public record AuditorDashboardSummary(
            long totalQuestionPools,
            long totalGeneratedPapers,
            long totalApprovalsRecorded,
            long totalPrintOperations,
            FreezeLevel activeFreezeLevel,
            long totalAnomaliesReported,
            List<SecurityIncidentDto> recentAnomalies,
            Instant timestamp
    ) {}
}

