package com.examchain.incident.replacement.service;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.incident.replacement.dto.ReplacementDtos.*;
import com.examchain.incident.replacement.entity.VariantReplacementEntity;
import com.examchain.incident.replacement.repository.VariantReplacementRepository;
import com.examchain.qr.dto.ProvenanceDtos.CreateTraceRequest;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.qr.service.PaperProvenanceService;
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
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

@Service
public class VariantReplacementService {

    private static final Logger log = LoggerFactory.getLogger(VariantReplacementService.class);

    private final VariantReplacementRepository replacementRepository;
    private final ReleaseScheduleRepository releaseScheduleRepository;
    private final PaperProvenanceRepository provenanceRepository;
    private final PaperProvenanceService provenanceService;
    private final DynamicPaperGenerationService generationService;
    private final FabricLedgerService ledgerService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public VariantReplacementService(
            VariantReplacementRepository replacementRepository,
            ReleaseScheduleRepository releaseScheduleRepository,
            PaperProvenanceRepository provenanceRepository,
            PaperProvenanceService provenanceService,
            DynamicPaperGenerationService generationService,
            FabricLedgerService ledgerService
    ) {
        this.replacementRepository = replacementRepository;
        this.releaseScheduleRepository = releaseScheduleRepository;
        this.provenanceRepository = provenanceRepository;
        this.provenanceService = provenanceService;
        this.generationService = generationService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public ReplacementResponse executeReplacement(ExecuteReplacementRequest request) {
        // 1. Verify quarantined paper status
        ReleaseScheduleEntity quarantinedSchedule = releaseScheduleRepository.findByPaperId(request.quarantinedPaperId())
                .orElseThrow(() -> new ResourceNotFoundException("No release schedule found for quarantined paper: " + request.quarantinedPaperId()));

        if (quarantinedSchedule.getStatus() != ReleaseStatus.QUARANTINED) {
            throw new IllegalStateException("Only quarantined variants can be replaced. Current status: " + quarantinedSchedule.getStatus());
        }

        // 2. Verify reserve paper exists
        GeneratedPaperResponse reservePaper = generationService.getPaperByPaperId(request.reservePaperId());

        Instant now = Instant.now();

        // 3. Mark quarantined variant as REPLACED (permanently revoking access)
        quarantinedSchedule.setStatus(ReleaseStatus.REPLACED);
        releaseScheduleRepository.save(quarantinedSchedule);

        provenanceRepository.findByPaperId(request.quarantinedPaperId()).ifPresent(prov -> {
            prov.setStatus(ProvenanceStatus.REPLACED);
            provenanceRepository.save(prov);
        });

        // 4. Activate reserve variant: create or update release schedule to RELEASED
        ReleaseScheduleEntity reserveSchedule = releaseScheduleRepository.findByPaperId(request.reservePaperId())
                .orElseGet(() -> new ReleaseScheduleEntity(
                        request.reservePaperId(),
                        reservePaper.subjectId(),
                        quarantinedSchedule.getRequiredThreshold(),
                        now,
                        quarantinedSchedule.getReleaseWindowEndTime()
                ));

        reserveSchedule.setCurrentApprovalCount(reserveSchedule.getRequiredThreshold());
        reserveSchedule.setStatus(ReleaseStatus.RELEASED);
        reserveSchedule.setReleasedAt(now);
        reserveSchedule.setReleasedBy(request.authorizedBy());
        releaseScheduleRepository.save(reserveSchedule);

        // 5. Ensure reserve paper has registered provenance trace
        if (provenanceRepository.findByPaperId(request.reservePaperId()).isEmpty()) {
            provenanceService.registerPaperTrace(new CreateTraceRequest(
                    request.reservePaperId(),
                    request.examCode(),
                    "Reserve Replacement for " + request.quarantinedPaperId(),
                    "ALL_CENTRES",
                    now,
                    quarantinedSchedule.getReleaseWindowEndTime() != null ? quarantinedSchedule.getReleaseWindowEndTime() : now.plus(3, ChronoUnit.HOURS)
            ));
        }

        // 6. Record on-chain ReplaceVariant transaction
        ledgerService.replaceVariant(request.quarantinedPaperId(), request.reservePaperId(), request.authorizedBy());

        // 7. Save replacement audit entity
        String replacementId = generateReplacementId();
        VariantReplacementEntity entity = new VariantReplacementEntity(
                replacementId,
                request.quarantinedPaperId(),
                request.reservePaperId(),
                request.examCode(),
                request.reason(),
                request.authorizedBy()
        );
        replacementRepository.save(entity);

        log.warn("VARIANT REPLACEMENT COMPLETED: [{}] -> [{}] authorized by [{}]",
                request.quarantinedPaperId(), request.reservePaperId(), request.authorizedBy());

        return new ReplacementResponse(
                replacementId,
                request.quarantinedPaperId(),
                request.reservePaperId(),
                request.examCode(),
                request.reason(),
                request.authorizedBy(),
                "COMPLETED",
                "RELEASED",
                now
        );
    }

    @Transactional(readOnly = true)
    public List<ReplacementResponse> getReplacementHistory() {
        return replacementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(e -> new ReplacementResponse(
                        e.getReplacementId(),
                        e.getQuarantinedPaperId(),
                        e.getReservePaperId(),
                        e.getExamCode(),
                        e.getReason(),
                        e.getAuthorizedBy(),
                        e.getStatus(),
                        "RELEASED",
                        e.getCreatedAt()
                ))
                .toList();
    }

    private String generateReplacementId() {
        byte[] bytes = new byte[4];
        secureRandom.nextBytes(bytes);
        return "RPL-" + HexFormat.of().formatHex(bytes).toUpperCase();
    }
}

