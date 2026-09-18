package com.examchain.question.entity;

import com.examchain.question.model.PoolStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "question_pools",
    uniqueConstraints = @UniqueConstraint(name = "uk_subject_pool", columnNames = {"subject_id", "pool_code"})
)
public class QuestionPoolEntity {

    @Id
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "pool_code", nullable = false, length = 50)
    private String poolCode;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PoolStatus status;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public QuestionPoolEntity() {}

    public QuestionPoolEntity(UUID id, UUID subjectId, String poolCode, String name, String description, PoolStatus status, String createdBy, String approvedBy, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.poolCode = poolCode;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID subjectId;
        private String poolCode;
        private String name;
        private String description;
        private PoolStatus status;
        private String createdBy;
        private String approvedBy;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder subjectId(UUID subjectId) { this.subjectId = subjectId; return this; }
        public Builder poolCode(String poolCode) { this.poolCode = poolCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(PoolStatus status) { this.status = status; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder approvedBy(String approvedBy) { this.approvedBy = approvedBy; return this; }

        public QuestionPoolEntity build() {
            QuestionPoolEntity entity = new QuestionPoolEntity();
            entity.id = this.id;
            entity.subjectId = this.subjectId;
            entity.poolCode = this.poolCode;
            entity.name = this.name;
            entity.description = this.description;
            entity.status = this.status != null ? this.status : PoolStatus.DRAFT;
            entity.createdBy = this.createdBy;
            entity.approvedBy = this.approvedBy;
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
            status = PoolStatus.DRAFT;
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

    public String getPoolCode() { return poolCode; }
    public void setPoolCode(String poolCode) { this.poolCode = poolCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PoolStatus getStatus() { return status; }
    public void setStatus(PoolStatus status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

