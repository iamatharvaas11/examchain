package com.examchain.offline.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "offline_authorization_tokens")
public class OfflineTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "token_id", nullable = false, unique = true, length = 64)
    private String tokenId;

    @Column(name = "centre_code", nullable = false, length = 64)
    private String centreCode;

    @Column(name = "paper_id", nullable = false, length = 64)
    private String paperId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "used_by_terminal", length = 64)
    private String usedByTerminal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public OfflineTokenEntity() {
        this.isUsed = false;
        this.createdAt = Instant.now();
    }

    public OfflineTokenEntity(
            String tokenId,
            String centreCode,
            String paperId,
            String tokenHash,
            Instant validFrom,
            Instant validUntil
    ) {
        this.tokenId = tokenId;
        this.centreCode = centreCode;
        this.paperId = paperId;
        this.tokenHash = tokenHash;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.isUsed = false;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getCentreCode() {
        return centreCode;
    }

    public void setCentreCode(String centreCode) {
        this.centreCode = centreCode;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public String getUsedByTerminal() {
        return usedByTerminal;
    }

    public void setUsedByTerminal(String usedByTerminal) {
        this.usedByTerminal = usedByTerminal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

