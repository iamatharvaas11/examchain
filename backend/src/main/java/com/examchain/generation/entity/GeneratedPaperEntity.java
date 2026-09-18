package com.examchain.generation.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generated_papers")
public class GeneratedPaperEntity {

    @Id
    private UUID id;

    @Column(name = "paper_id", nullable = false, unique = true, length = 50)
    private String paperId;

    @Column(name = "blueprint_id", nullable = false)
    private UUID blueprintId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "set_code", nullable = false, length = 50)
    private String setCode;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "paper_hash", nullable = false, length = 64)
    private String paperHash;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GeneratedPaperEntity() {}

    public GeneratedPaperEntity(UUID id, String paperId, UUID blueprintId, UUID subjectId, String setCode, int totalMarks, int questionCount, String paperHash, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.paperId = paperId;
        this.blueprintId = blueprintId;
        this.subjectId = subjectId;
        this.setCode = setCode;
        this.totalMarks = totalMarks;
        this.questionCount = questionCount;
        this.paperHash = paperHash;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String paperId;
        private UUID blueprintId;
        private UUID subjectId;
        private String setCode;
        private int totalMarks;
        private int questionCount;
        private String paperHash;
        private String status;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder paperId(String paperId) { this.paperId = paperId; return this; }
        public Builder blueprintId(UUID blueprintId) { this.blueprintId = blueprintId; return this; }
        public Builder subjectId(UUID subjectId) { this.subjectId = subjectId; return this; }
        public Builder setCode(String setCode) { this.setCode = setCode; return this; }
        public Builder totalMarks(int totalMarks) { this.totalMarks = totalMarks; return this; }
        public Builder questionCount(int questionCount) { this.questionCount = questionCount; return this; }
        public Builder paperHash(String paperHash) { this.paperHash = paperHash; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public GeneratedPaperEntity build() {
            GeneratedPaperEntity entity = new GeneratedPaperEntity();
            entity.id = this.id;
            entity.paperId = this.paperId;
            entity.blueprintId = this.blueprintId;
            entity.subjectId = this.subjectId;
            entity.setCode = this.setCode;
            entity.totalMarks = this.totalMarks;
            entity.questionCount = this.questionCount;
            entity.paperHash = this.paperHash;
            entity.status = this.status != null ? this.status : "GENERATED";
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
            status = "GENERATED";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPaperId() { return paperId; }
    public void setPaperId(String paperId) { this.paperId = paperId; }

    public UUID getBlueprintId() { return blueprintId; }
    public void setBlueprintId(UUID blueprintId) { this.blueprintId = blueprintId; }

    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }

    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public String getPaperHash() { return paperHash; }
    public void setPaperHash(String paperHash) { this.paperHash = paperHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

