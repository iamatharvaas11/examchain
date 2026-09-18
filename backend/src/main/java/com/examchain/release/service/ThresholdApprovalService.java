package com.examchain.release.service;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.release.dto.ReleaseDtos.*;
import com.examchain.release.entity.PaperApprovalEntity;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.PaperApprovalRepository;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class ThresholdApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ThresholdApprovalService.class);

    private final ReleaseScheduleRepository scheduleRepository;
    private final PaperApprovalRepository approvalRepository;
    private final FabricLedgerService ledgerService;

    @Autowired
    public ThresholdApprovalService(
            ReleaseScheduleRepository scheduleRepository,
            PaperApprovalRepository approvalRepository,
            FabricLedgerService ledgerService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.approvalRepository = approvalRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public ReleaseStatusResponse createSchedule(ScheduleReleaseRequest request) {
        ReleaseScheduleEntity schedule = scheduleRepository.findByPaperId(request.paperId())
                .orElseGet(() -> new ReleaseScheduleEntity(
                        request.paperId(),
                        request.examId(),
                        request.requiredThreshold(),
                        request.scheduledReleaseTime(),
                        request.releaseWindowEndTime()
                ));

        schedule.setRequiredThreshold(request.requiredThreshold());
        schedule.setScheduledReleaseTime(request.scheduledReleaseTime());
        schedule.setReleaseWindowEndTime(request.releaseWindowEndTime());

        ReleaseScheduleEntity saved = scheduleRepository.save(schedule);
        log.info("Configured release schedule for paper [{}] with threshold [{}] and release time [{}]",
                saved.getPaperId(), saved.getRequiredThreshold(), saved.getScheduledReleaseTime());

        return getReleaseStatus(saved.getPaperId());
    }

    @Transactional
    public ReleaseStatusResponse submitApproval(String paperId, SubmitApprovalRequest request) {
        ReleaseScheduleEntity schedule = scheduleRepository.findByPaperId(paperId)
                .orElseThrow(() -> new ResourceNotFoundException("No release schedule found for paper: " + paperId));

        if (schedule.getStatus() == ReleaseStatus.QUARANTINED) {
            throw new IllegalStateException("Cannot approve quarantined paper variant: " + paperId);
        }

        if (approvalRepository.findByPaperIdAndAuthorityId(paperId, request.authorityId()).isPresent()) {
            throw new IllegalArgumentException("Authority [" + request.authorityId() + "] has already submitted approval for paper [" + paperId + "]");
        }

        String signatureHash = computeSignatureHash(paperId, request.authorityId(), request.authorityRole());
        PaperApprovalEntity approval = new PaperApprovalEntity(
                paperId,
                request.authorityId(),
                request.authorityRole(),
                request.authorityName(),
                signatureHash,
                request.comments()
        );
        approvalRepository.save(approval);

        int newCount = schedule.getCurrentApprovalCount() + 1;
        schedule.setCurrentApprovalCount(newCount);

        if (newCount >= schedule.getRequiredThreshold() && schedule.getStatus() == ReleaseStatus.PENDING_APPROVAL) {
            schedule.setStatus(ReleaseStatus.THRESHOLD_MET);
            log.info("Paper [{}] reached quorum approval threshold ({}/{})",
                    paperId, newCount, schedule.getRequiredThreshold());
        }
        scheduleRepository.save(schedule);

        // Record approval on Fabric ledger
        ledgerService.recordApproval(paperId, request.authorityId(), request.authorityRole());

        return getReleaseStatus(paperId);
    }

    @Transactional
    public ReleaseStatusResponse authorizeRelease(String paperId, String authorizedBy) {
        ReleaseScheduleEntity schedule = scheduleRepository.findByPaperId(paperId)
                .orElseThrow(() -> new ResourceNotFoundException("No release schedule found for paper: " + paperId));

        Instant now = Instant.now();

        // 1. Quorum threshold check
        if (schedule.getCurrentApprovalCount() < schedule.getRequiredThreshold()) {
            throw new IllegalStateException(String.format(
                    "Release blocked: quorum threshold not met. Requires %d approvals, but only %d recorded.",
                    schedule.getRequiredThreshold(), schedule.getCurrentApprovalCount()
            ));
        }

        // 2. Time-lock schedule check
        if (now.isBefore(schedule.getScheduledReleaseTime())) {
            throw new IllegalStateException(String.format(
                    "Release blocked: time-lock release window has not opened yet. Scheduled at: %s, Current time: %s",
                    schedule.getScheduledReleaseTime(), now
            ));
        }

        // 3. Expiration check
        if (now.isAfter(schedule.getReleaseWindowEndTime())) {
            schedule.setStatus(ReleaseStatus.EXPIRED);
            scheduleRepository.save(schedule);
            throw new IllegalStateException("Release blocked: release window expired at " + schedule.getReleaseWindowEndTime());
        }

        // 4. Quarantine check
        if (schedule.getStatus() == ReleaseStatus.QUARANTINED) {
            throw new IllegalStateException("Release blocked: paper variant is quarantined due to a security incident.");
        }

        schedule.setStatus(ReleaseStatus.RELEASED);
        schedule.setReleasedAt(now);
        schedule.setReleasedBy(authorizedBy);
        scheduleRepository.save(schedule);

        log.info("Paper [{}] authorized for release by [{}] at [{}]", paperId, authorizedBy, now);

        // Commit release authorization to Fabric ledger
        ledgerService.authorizeRelease(paperId, authorizedBy);

        return getReleaseStatus(paperId);
    }

    @Transactional(readOnly = true)
    public ReleaseStatusResponse getReleaseStatus(String paperId) {
        ReleaseScheduleEntity schedule = scheduleRepository.findByPaperId(paperId)
                .orElseThrow(() -> new ResourceNotFoundException("No release schedule found for paper: " + paperId));

        List<ApprovalSummaryDto> approvals = approvalRepository.findByPaperId(paperId).stream()
                .map(a -> new ApprovalSummaryDto(
                        a.getAuthorityId(),
                        a.getAuthorityRole(),
                        a.getAuthorityName(),
                        a.getSignatureHash(),
                        a.getCreatedAt()
                ))
                .toList();

        Instant now = Instant.now();
        boolean canReleaseNow = (schedule.getCurrentApprovalCount() >= schedule.getRequiredThreshold())
                && !now.isBefore(schedule.getScheduledReleaseTime())
                && !now.isAfter(schedule.getReleaseWindowEndTime())
                && schedule.getStatus() != ReleaseStatus.QUARANTINED
                && schedule.getStatus() != ReleaseStatus.RELEASED;

        return new ReleaseStatusResponse(
                schedule.getPaperId(),
                schedule.getRequiredThreshold(),
                schedule.getCurrentApprovalCount(),
                schedule.getScheduledReleaseTime(),
                schedule.getReleaseWindowEndTime(),
                schedule.getStatus(),
                canReleaseNow,
                schedule.getReleasedAt(),
                schedule.getReleasedBy(),
                approvals
        );
    }

    private String computeSignatureHash(String paperId, String authId, String role) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = paperId + ":" + authId + ":" + role + ":" + Instant.now().toEpochMilli();
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

