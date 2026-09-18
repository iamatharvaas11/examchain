package com.examchain.incident.replacement.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "variant_replacements")
public class VariantReplacementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "replacement_id", nullable = false, unique = true, length = 64)
    private String replacementId;

    @Column(name = "quarantined_paper_id", nullable = false, length = 64)
    private String quarantinedPaperId;

    @Column(name = "reserve_paper_id", nullable = false, length = 64)
    private String reservePaperId;

    @Column(name = "exam_code", nullable = false, length = 64)
    private String examCode;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "authorized_by", nullable = false, length = 64)
    private String authorizedBy;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public VariantReplacementEntity() {
        this.status = "COMPLETED";
        this.createdAt = Instant.now();
    }

    public VariantReplacementEntity(
            String replacementId,
            String quarantinedPaperId,
            String reservePaperId,
            String examCode,
            String reason,
            String authorizedBy
    ) {
        this.replacementId = replacementId;
        this.quarantinedPaperId = quarantinedPaperId;
        this.reservePaperId = reservePaperId;
        this.examCode = examCode;
        this.reason = reason;
        this.authorizedBy = authorizedBy;
        this.status = "COMPLETED";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReplacementId() {
        return replacementId;
    }

    public void setReplacementId(String replacementId) {
        this.replacementId = replacementId;
    }

    public String getQuarantinedPaperId() {
        return quarantinedPaperId;
    }

    public void setQuarantinedPaperId(String quarantinedPaperId) {
        this.quarantinedPaperId = quarantinedPaperId;
    }

    public String getReservePaperId() {
        return reservePaperId;
    }

    public void setReservePaperId(String reservePaperId) {
        this.reservePaperId = reservePaperId;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

