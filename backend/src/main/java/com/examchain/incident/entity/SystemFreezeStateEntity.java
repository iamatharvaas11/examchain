package com.examchain.incident.entity;

import com.examchain.incident.model.FreezeLevel;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "system_freeze_state")
public class SystemFreezeStateEntity {

    @Id
    private Integer id = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "freeze_level", nullable = false, length = 32)
    private FreezeLevel freezeLevel;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "triggered_by", length = 64)
    private String triggeredBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SystemFreezeStateEntity() {
        this.id = 1;
        this.freezeLevel = FreezeLevel.NORMAL;
        this.reason = "Default state";
        this.triggeredBy = "SYSTEM";
        this.updatedAt = Instant.now();
    }

    public SystemFreezeStateEntity(FreezeLevel freezeLevel, String reason, String triggeredBy) {
        this.id = 1;
        this.freezeLevel = freezeLevel;
        this.reason = reason;
        this.triggeredBy = triggeredBy;
        this.updatedAt = Instant.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FreezeLevel getFreezeLevel() {
        return freezeLevel;
    }

    public void setFreezeLevel(FreezeLevel freezeLevel) {
        this.freezeLevel = freezeLevel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

