package com.examchain.incident.entity;

import com.examchain.incident.model.IncidentEventType;
import com.examchain.incident.model.IncidentSeverity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_incident_events")
public class SecurityIncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "incident_id", nullable = false, unique = true, length = 64)
    private String incidentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private IncidentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private IncidentSeverity severity;

    @Column(name = "paper_id", length = 64)
    private String paperId;

    @Column(name = "centre_code", length = 64)
    private String centreCode;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "is_quarantined", nullable = false)
    private boolean isQuarantined;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SecurityIncidentEntity() {
        this.createdAt = Instant.now();
        this.isQuarantined = false;
    }

    public SecurityIncidentEntity(
            String incidentId,
            IncidentEventType eventType,
            IncidentSeverity severity,
            String paperId,
            String centreCode,
            String operatorId,
            String details,
            boolean isQuarantined
    ) {
        this.incidentId = incidentId;
        this.eventType = eventType;
        this.severity = severity;
        this.paperId = paperId;
        this.centreCode = centreCode;
        this.operatorId = operatorId;
        this.details = details;
        this.isQuarantined = isQuarantined;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public IncidentEventType getEventType() {
        return eventType;
    }

    public void setEventType(IncidentEventType eventType) {
        this.eventType = eventType;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
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

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isQuarantined() {
        return isQuarantined;
    }

    public void setQuarantined(boolean quarantined) {
        isQuarantined = quarantined;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

