package com.examchain.release.entity;

import com.examchain.release.model.ReleaseStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "release_schedules")
public class ReleaseScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "paper_id", nullable = false, unique = true, length = 64)
    private String paperId;

    @Column(name = "exam_id")
    private UUID examId;

    @Column(name = "required_threshold", nullable = false)
    private int requiredThreshold;

    @Column(name = "current_approval_count", nullable = false)
    private int currentApprovalCount;

    @Column(name = "scheduled_release_time", nullable = false)
    private Instant scheduledReleaseTime;

    @Column(name = "release_window_end_time", nullable = false)
    private Instant releaseWindowEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReleaseStatus status;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "released_by", length = 64)
    private String releasedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ReleaseScheduleEntity() {
        this.createdAt = Instant.now();
        this.requiredThreshold = 2;
        this.currentApprovalCount = 0;
        this.status = ReleaseStatus.PENDING_APPROVAL;
    }

    public ReleaseScheduleEntity(
            String paperId,
            UUID examId,
            int requiredThreshold,
            Instant scheduledReleaseTime,
            Instant releaseWindowEndTime
    ) {
        this.paperId = paperId;
        this.examId = examId;
        this.requiredThreshold = requiredThreshold > 0 ? requiredThreshold : 2;
        this.currentApprovalCount = 0;
        this.scheduledReleaseTime = scheduledReleaseTime;
        this.releaseWindowEndTime = releaseWindowEndTime;
        this.status = ReleaseStatus.PENDING_APPROVAL;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public int getRequiredThreshold() {
        return requiredThreshold;
    }

    public void setRequiredThreshold(int requiredThreshold) {
        this.requiredThreshold = requiredThreshold;
    }

    public int getCurrentApprovalCount() {
        return currentApprovalCount;
    }

    public void setCurrentApprovalCount(int currentApprovalCount) {
        this.currentApprovalCount = currentApprovalCount;
    }

    public Instant getScheduledReleaseTime() {
        return scheduledReleaseTime;
    }

    public void setScheduledReleaseTime(Instant scheduledReleaseTime) {
        this.scheduledReleaseTime = scheduledReleaseTime;
    }

    public Instant getReleaseWindowEndTime() {
        return releaseWindowEndTime;
    }

    public void setReleaseWindowEndTime(Instant releaseWindowEndTime) {
        this.releaseWindowEndTime = releaseWindowEndTime;
    }

    public ReleaseStatus getStatus() {
        return status;
    }

    public void setStatus(ReleaseStatus status) {
        this.status = status;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getReleasedBy() {
        return releasedBy;
    }

    public void setReleasedBy(String releasedBy) {
        this.releasedBy = releasedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

