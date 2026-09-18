package com.examchain.release;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.release.dto.ReleaseDtos.*;
import com.examchain.release.entity.PaperApprovalEntity;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.PaperApprovalRepository;
import com.examchain.release.repository.ReleaseScheduleRepository;
import com.examchain.release.service.ThresholdApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThresholdApprovalTest {

    private ReleaseScheduleRepository scheduleRepository;
    private PaperApprovalRepository approvalRepository;
    private FabricLedgerService ledgerService;
    private ThresholdApprovalService service;

    private final List<PaperApprovalEntity> savedApprovals = new ArrayList<>();
    private ReleaseScheduleEntity mockSchedule;

    @BeforeEach
    void setUp() {
        scheduleRepository = Mockito.mock(ReleaseScheduleRepository.class);
        approvalRepository = Mockito.mock(PaperApprovalRepository.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);
        savedApprovals.clear();

        mockSchedule = new ReleaseScheduleEntity(
                "PAP-2026-PHY",
                null,
                2, // 2-of-3 threshold
                Instant.now().plus(1, ChronoUnit.HOURS), // Future release time
                Instant.now().plus(4, ChronoUnit.HOURS)
        );

        when(scheduleRepository.findByPaperId("PAP-2026-PHY")).thenReturn(Optional.of(mockSchedule));
        when(scheduleRepository.save(any(ReleaseScheduleEntity.class))).thenAnswer(i -> i.getArgument(0));

        when(approvalRepository.findByPaperId("PAP-2026-PHY")).thenReturn(savedApprovals);
        when(approvalRepository.save(any(PaperApprovalEntity.class))).thenAnswer(i -> {
            PaperApprovalEntity a = i.getArgument(0);
            savedApprovals.add(a);
            return a;
        });

        service = new ThresholdApprovalService(scheduleRepository, approvalRepository, ledgerService);
    }

    @Test
    @DisplayName("Single authority approval does not meet 2-of-3 threshold")
    void testSingleApproval_StatusRemainsPending() {
        when(approvalRepository.findByPaperIdAndAuthorityId("PAP-2026-PHY", "AUTH-01")).thenReturn(Optional.empty());

        SubmitApprovalRequest req = new SubmitApprovalRequest("AUTH-01", "EXAM_AUTHORITY", "Dr. Alice", "Approved");
        ReleaseStatusResponse res = service.submitApproval("PAP-2026-PHY", req);

        assertThat(res.currentApprovalCount()).isEqualTo(1);
        assertThat(res.requiredThreshold()).isEqualTo(2);
        assertThat(res.status()).isEqualTo(ReleaseStatus.PENDING_APPROVAL);
        assertThat(res.canReleaseNow()).isFalse();

        verify(ledgerService, times(1)).recordApproval("PAP-2026-PHY", "AUTH-01", "EXAM_AUTHORITY");
    }

    @Test
    @DisplayName("Duplicate approval from same authority is rejected")
    void testDuplicateApproval_Rejected() {
        PaperApprovalEntity existing = new PaperApprovalEntity("PAP-2026-PHY", "AUTH-01", "EXAM_AUTHORITY", "Dr. Alice", "hash", null);
        when(approvalRepository.findByPaperIdAndAuthorityId("PAP-2026-PHY", "AUTH-01")).thenReturn(Optional.of(existing));

        SubmitApprovalRequest req = new SubmitApprovalRequest("AUTH-01", "EXAM_AUTHORITY", "Dr. Alice", "Second try");

        assertThatThrownBy(() -> service.submitApproval("PAP-2026-PHY", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already submitted approval");
    }

    @Test
    @DisplayName("Second authority approval reaches quorum threshold (THRESHOLD_MET)")
    void testSecondApproval_MeetsThreshold() {
        mockSchedule.setCurrentApprovalCount(1);
        when(approvalRepository.findByPaperIdAndAuthorityId("PAP-2026-PHY", "AUTH-02")).thenReturn(Optional.empty());

        SubmitApprovalRequest req = new SubmitApprovalRequest("AUTH-02", "CONTROLLER", "Dr. Bob", "Approved");
        ReleaseStatusResponse res = service.submitApproval("PAP-2026-PHY", req);

        assertThat(res.currentApprovalCount()).isEqualTo(2);
        assertThat(res.status()).isEqualTo(ReleaseStatus.THRESHOLD_MET);
    }

    @Test
    @DisplayName("Release is blocked before scheduled time-lock window opens even if threshold met")
    void testEarlyRelease_BlockedByTimeLock() {
        mockSchedule.setCurrentApprovalCount(2);
        mockSchedule.setStatus(ReleaseStatus.THRESHOLD_MET);
        // scheduledReleaseTime is in future (1 hour from now)

        assertThatThrownBy(() -> service.authorizeRelease("PAP-2026-PHY", "CONTROLLER-CHIEF"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time-lock release window has not opened yet");
    }

    @Test
    @DisplayName("Release is blocked if quorum threshold not met even if time-lock reached")
    void testRelease_BlockedByBelowThreshold() {
        mockSchedule.setCurrentApprovalCount(1); // below required 2
        mockSchedule.setScheduledReleaseTime(Instant.now().minus(10, ChronoUnit.MINUTES)); // past release time

        assertThatThrownBy(() -> service.authorizeRelease("PAP-2026-PHY", "CONTROLLER-CHIEF"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quorum threshold not met");
    }

    @Test
    @DisplayName("Release succeeds when quorum threshold is met AND release time is reached")
    void testRelease_SucceedsWhenThresholdAndScheduleMet() {
        mockSchedule.setCurrentApprovalCount(2);
        mockSchedule.setStatus(ReleaseStatus.THRESHOLD_MET);
        mockSchedule.setScheduledReleaseTime(Instant.now().minus(10, ChronoUnit.MINUTES)); // past release time

        ReleaseStatusResponse res = service.authorizeRelease("PAP-2026-PHY", "CONTROLLER-CHIEF");

        assertThat(res.status()).isEqualTo(ReleaseStatus.RELEASED);
        assertThat(res.releasedBy()).isEqualTo("CONTROLLER-CHIEF");
        assertThat(res.releasedAt()).isNotNull();

        verify(ledgerService, times(1)).authorizeRelease("PAP-2026-PHY", "CONTROLLER-CHIEF");
    }

    @Test
    @DisplayName("Quarantined paper release is strictly blocked")
    void testQuarantinedPaper_ReleaseBlocked() {
        mockSchedule.setCurrentApprovalCount(3);
        mockSchedule.setStatus(ReleaseStatus.QUARANTINED);
        mockSchedule.setScheduledReleaseTime(Instant.now().minus(10, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> service.authorizeRelease("PAP-2026-PHY", "CONTROLLER-CHIEF"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("paper variant is quarantined");
    }
}

