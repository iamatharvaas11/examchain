package com.examchain.qr.entity;

import com.examchain.qr.model.ProvenanceStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_provenance")
public class PaperProvenanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "trace_id", nullable = false, unique = true, length = 64)
    private String traceId;

    @Column(name = "paper_id", nullable = false, length = 64)
    private String paperId;

    @Column(name = "paper_hash", nullable = false, length = 64)
    private String paperHash;

    @Column(name = "exam_code", nullable = false, length = 64)
    private String examCode;

    @Column(name = "exam_title", nullable = false)
    private String examTitle;

    @Column(name = "centre_code", length = 64)
    private String centreCode;

    @Column(name = "exam_start_time", nullable = false)
    private Instant examStartTime;

    @Column(name = "exam_end_time", nullable = false)
    private Instant examEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProvenanceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PaperProvenanceEntity() {
        this.createdAt = Instant.now();
        this.status = ProvenanceStatus.LOCKED;
    }

    public PaperProvenanceEntity(
            String traceId,
            String paperId,
            String paperHash,
            String examCode,
            String examTitle,
            String centreCode,
            Instant examStartTime,
            Instant examEndTime
    ) {
        this.traceId = traceId;
        this.paperId = paperId;
        this.paperHash = paperHash;
        this.examCode = examCode;
        this.examTitle = examTitle;
        this.centreCode = centreCode;
        this.examStartTime = examStartTime;
        this.examEndTime = examEndTime;
        this.status = ProvenanceStatus.LOCKED;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperHash() {
        return paperHash;
    }

    public void setPaperHash(String paperHash) {
        this.paperHash = paperHash;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getCentreCode() {
        return centreCode;
    }

    public void setCentreCode(String centreCode) {
        this.centreCode = centreCode;
    }

    public Instant getExamStartTime() {
        return examStartTime;
    }

    public void setExamStartTime(Instant examStartTime) {
        this.examStartTime = examStartTime;
    }

    public Instant getExamEndTime() {
        return examEndTime;
    }

    public void setExamEndTime(Instant examEndTime) {
        this.examEndTime = examEndTime;
    }

    public ProvenanceStatus getStatus() {
        return status;
    }

    public void setStatus(ProvenanceStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

