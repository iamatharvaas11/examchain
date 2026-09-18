package com.examchain.exam.entity;

import com.examchain.exam.model.ExamStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exams")
public class ExamEntity {

    @Id
    private UUID id;

    @Column(name = "exam_code", nullable = false, unique = true, length = 50)
    private String examCode;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_session", nullable = false, length = 50)
    private String academicSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExamStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ExamEntity() {}

    public ExamEntity(UUID id, String examCode, String title, String description, String academicSession, ExamStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.examCode = examCode;
        this.title = title;
        this.description = description;
        this.academicSession = academicSession;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String examCode;
        private String title;
        private String description;
        private String academicSession;
        private ExamStatus status;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder examCode(String examCode) { this.examCode = examCode; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder academicSession(String academicSession) { this.academicSession = academicSession; return this; }
        public Builder status(ExamStatus status) { this.status = status; return this; }

        public ExamEntity build() {
            ExamEntity entity = new ExamEntity();
            entity.id = this.id;
            entity.examCode = this.examCode;
            entity.title = this.title;
            entity.description = this.description;
            entity.academicSession = this.academicSession;
            entity.status = this.status != null ? this.status : ExamStatus.DRAFT;
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
            status = ExamStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAcademicSession() { return academicSession; }
    public void setAcademicSession(String academicSession) { this.academicSession = academicSession; }

    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

