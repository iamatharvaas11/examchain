package com.examchain.generation.entity;

import com.examchain.generation.model.PolicyMode;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blueprints")
public class BlueprintEntity {

    @Id
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "blueprint_code", nullable = false, unique = true, length = 50)
    private String blueprintCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_mode", nullable = false, length = 50)
    private PolicyMode policyMode;

    @Column(name = "rules_json", nullable = false, columnDefinition = "TEXT")
    private String rulesJson;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BlueprintEntity() {}

    public BlueprintEntity(UUID id, UUID subjectId, String blueprintCode, String name, int totalMarks, int durationMinutes, PolicyMode policyMode, String rulesJson, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.blueprintCode = blueprintCode;
        this.name = name;
        this.totalMarks = totalMarks;
        this.durationMinutes = durationMinutes;
        this.policyMode = policyMode;
        this.rulesJson = rulesJson;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID subjectId;
        private String blueprintCode;
        private String name;
        private int totalMarks;
        private int durationMinutes;
        private PolicyMode policyMode;
        private String rulesJson;
        private String status;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder subjectId(UUID subjectId) { this.subjectId = subjectId; return this; }
        public Builder blueprintCode(String blueprintCode) { this.blueprintCode = blueprintCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder totalMarks(int totalMarks) { this.totalMarks = totalMarks; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder policyMode(PolicyMode policyMode) { this.policyMode = policyMode; return this; }
        public Builder rulesJson(String rulesJson) { this.rulesJson = rulesJson; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public BlueprintEntity build() {
            BlueprintEntity entity = new BlueprintEntity();
            entity.id = this.id;
            entity.subjectId = this.subjectId;
            entity.blueprintCode = this.blueprintCode;
            entity.name = this.name;
            entity.totalMarks = this.totalMarks;
            entity.durationMinutes = this.durationMinutes;
            entity.policyMode = this.policyMode != null ? this.policyMode : PolicyMode.DYNAMIC_MULTI_POOL;
            entity.rulesJson = this.rulesJson;
            entity.status = this.status != null ? this.status : "ACTIVE";
            return entity;
        }
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }

    public String getBlueprintCode() { return blueprintCode; }
    public void setBlueprintCode(String blueprintCode) { this.blueprintCode = blueprintCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public PolicyMode getPolicyMode() { return policyMode; }
    public void setPolicyMode(PolicyMode policyMode) { this.policyMode = policyMode; }

    public String getRulesJson() { return rulesJson; }
    public void setRulesJson(String rulesJson) { this.rulesJson = rulesJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

