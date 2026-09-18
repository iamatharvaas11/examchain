package com.examchain.centre.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "print_audit_logs")
public class PrintAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "paper_id", nullable = false, length = 64)
    private String paperId;

    @Column(name = "centre_code", nullable = false, length = 64)
    private String centreCode;

    @Column(name = "terminal_id", nullable = false, length = 64)
    private String terminalId;

    @Column(name = "operator_id", nullable = false, length = 64)
    private String operatorId;

    @Column(name = "device_fingerprint", nullable = false, length = 128)
    private String deviceFingerprint;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "watermark_text", nullable = false)
    private String watermarkText;

    @Column(name = "copy_count", nullable = false)
    private int copyCount;

    @Column(name = "receipt_id", nullable = false, unique = true, length = 64)
    private String receiptId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PrintAuditLogEntity() {
        this.createdAt = Instant.now();
    }

    public PrintAuditLogEntity(
            String paperId,
            String centreCode,
            String terminalId,
            String operatorId,
            String deviceFingerprint,
            int riskScore,
            String watermarkText,
            int copyCount,
            String receiptId,
            String status
    ) {
        this.paperId = paperId;
        this.centreCode = centreCode;
        this.terminalId = terminalId;
        this.operatorId = operatorId;
        this.deviceFingerprint = deviceFingerprint;
        this.riskScore = riskScore;
        this.watermarkText = watermarkText;
        this.copyCount = copyCount;
        this.receiptId = receiptId;
        this.status = status;
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

    public String getCentreCode() {
        return centreCode;
    }

    public void setCentreCode(String centreCode) {
        this.centreCode = centreCode;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public int getCopyCount() {
        return copyCount;
    }

    public void setCopyCount(int copyCount) {
        this.copyCount = copyCount;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
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

