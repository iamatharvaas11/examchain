package com.examchain.exam.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "subjects",
    uniqueConstraints = @UniqueConstraint(name = "uk_exam_subject", columnNames = {"exam_id", "subject_code"})
)
public class SubjectEntity {

    @Id
    private UUID id;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "subject_code", nullable = false, length = 50)
    private String subjectCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "passing_marks", nullable = false)
    private int passingMarks;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SubjectEntity() {}

    public SubjectEntity(UUID id, UUID examId, String subjectCode, String name, int totalMarks, int passingMarks, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.examId = examId;
        this.subjectCode = subjectCode;
        this.name = name;
        this.totalMarks = totalMarks;
        this.passingMarks = passingMarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID examId;
        private String subjectCode;
        private String name;
        private int totalMarks;
        private int passingMarks;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder examId(UUID examId) { this.examId = examId; return this; }
        public Builder subjectCode(String subjectCode) { this.subjectCode = subjectCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder totalMarks(int totalMarks) { this.totalMarks = totalMarks; return this; }
        public Builder passingMarks(int passingMarks) { this.passingMarks = passingMarks; return this; }

        public SubjectEntity build() {
            SubjectEntity entity = new SubjectEntity();
            entity.id = this.id;
            entity.examId = this.examId;
            entity.subjectCode = this.subjectCode;
            entity.name = this.name;
            entity.totalMarks = this.totalMarks;
            entity.passingMarks = this.passingMarks;
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
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getPassingMarks() { return passingMarks; }
    public void setPassingMarks(int passingMarks) { this.passingMarks = passingMarks; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

