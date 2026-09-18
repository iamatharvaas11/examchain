package com.examchain.exam.entity;

import com.examchain.exam.model.AssignmentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "setter_assignments",
    uniqueConstraints = @UniqueConstraint(name = "uk_subject_setter", columnNames = {"subject_id", "setter_id"})
)
public class SetterAssignmentEntity {

    @Id
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "setter_id", nullable = false, length = 100)
    private String setterId;

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssignmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SetterAssignmentEntity() {}

    public SetterAssignmentEntity(UUID id, UUID subjectId, String setterId, String assignedBy, AssignmentStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.setterId = setterId;
        this.assignedBy = assignedBy;
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
        private String setterId;
        private String assignedBy;
        private AssignmentStatus status;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder subjectId(UUID subjectId) { this.subjectId = subjectId; return this; }
        public Builder setterId(String setterId) { this.setterId = setterId; return this; }
        public Builder assignedBy(String assignedBy) { this.assignedBy = assignedBy; return this; }
        public Builder status(AssignmentStatus status) { this.status = status; return this; }

        public SetterAssignmentEntity build() {
            SetterAssignmentEntity entity = new SetterAssignmentEntity();
            entity.id = this.id;
            entity.subjectId = this.subjectId;
            entity.setterId = this.setterId;
            entity.assignedBy = this.assignedBy;
            entity.status = this.status != null ? this.status : AssignmentStatus.ACTIVE;
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
            status = AssignmentStatus.ACTIVE;
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

    public String getSetterId() { return setterId; }
    public void setSetterId(String setterId) { this.setterId = setterId; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

