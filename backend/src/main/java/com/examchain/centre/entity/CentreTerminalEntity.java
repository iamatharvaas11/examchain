package com.examchain.centre.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "centre_terminals", uniqueConstraints = {
        @UniqueConstraint(name = "uq_centre_terminal", columnNames = {"centre_code", "terminal_id"})
})
public class CentreTerminalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "centre_code", nullable = false, length = 64)
    private String centreCode;

    @Column(name = "terminal_id", nullable = false, length = 64)
    private String terminalId;

    @Column(name = "registered_fingerprint", nullable = false, length = 128)
    private String registeredFingerprint;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // TRUSTED, FLAGGED, BLOCKED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public CentreTerminalEntity() {
        this.status = "TRUSTED";
        this.createdAt = Instant.now();
    }

    public CentreTerminalEntity(String centreCode, String terminalId, String registeredFingerprint) {
        this.centreCode = centreCode;
        this.terminalId = terminalId;
        this.registeredFingerprint = registeredFingerprint;
        this.status = "TRUSTED";
        this.lastSeenAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getRegisteredFingerprint() {
        return registeredFingerprint;
    }

    public void setRegisteredFingerprint(String registeredFingerprint) {
        this.registeredFingerprint = registeredFingerprint;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
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

