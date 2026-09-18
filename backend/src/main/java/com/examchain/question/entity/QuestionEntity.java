package com.examchain.question.entity;

import com.examchain.question.model.CognitiveLevel;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionStatus;
import com.examchain.question.model.QuestionType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false, unique = true, length = 50)
    private String questionId;

    @Column(name = "pool_id", nullable = false)
    private UUID poolId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(nullable = false)
    private int unit;

    @Column(nullable = false)
    private int marks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", nullable = false, length = 30)
    private CognitiveLevel cognitiveLevel;

    @Column(name = "setter_id", nullable = false, length = 100)
    private String setterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionStatus status;

    @Column(nullable = false)
    private int version;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public QuestionEntity() {}

    public QuestionEntity(UUID id, String questionId, UUID poolId, UUID subjectId, int unit, int marks,
                          DifficultyLevel difficulty, QuestionType questionType, CognitiveLevel cognitiveLevel,
                          String setterId, QuestionStatus status, int version, String contentJson,
                          String hash, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.questionId = questionId;
        this.poolId = poolId;
        this.subjectId = subjectId;
        this.unit = unit;
        this.marks = marks;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.cognitiveLevel = cognitiveLevel;
        this.setterId = setterId;
        this.status = status;
        this.version = version;
        this.contentJson = contentJson;
        this.hash = hash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String questionId;
        private UUID poolId;
        private UUID subjectId;
        private int unit;
        private int marks;
        private DifficultyLevel difficulty;
        private QuestionType questionType;
        private CognitiveLevel cognitiveLevel;
        private String setterId;
        private QuestionStatus status;
        private int version;
        private String contentJson;
        private String hash;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder questionId(String questionId) { this.questionId = questionId; return this; }
        public Builder poolId(UUID poolId) { this.poolId = poolId; return this; }
        public Builder subjectId(UUID subjectId) { this.subjectId = subjectId; return this; }
        public Builder unit(int unit) { this.unit = unit; return this; }
        public Builder marks(int marks) { this.marks = marks; return this; }
        public Builder difficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; return this; }
        public Builder questionType(QuestionType questionType) { this.questionType = questionType; return this; }
        public Builder cognitiveLevel(CognitiveLevel cognitiveLevel) { this.cognitiveLevel = cognitiveLevel; return this; }
        public Builder setterId(String setterId) { this.setterId = setterId; return this; }
        public Builder status(QuestionStatus status) { this.status = status; return this; }
        public Builder version(int version) { this.version = version; return this; }
        public Builder contentJson(String contentJson) { this.contentJson = contentJson; return this; }
        public Builder hash(String hash) { this.hash = hash; return this; }

        public QuestionEntity build() {
            QuestionEntity entity = new QuestionEntity();
            entity.id = this.id;
            entity.questionId = this.questionId;
            entity.poolId = this.poolId;
            entity.subjectId = this.subjectId;
            entity.unit = this.unit;
            entity.marks = this.marks;
            entity.difficulty = this.difficulty;
            entity.questionType = this.questionType;
            entity.cognitiveLevel = this.cognitiveLevel;
            entity.setterId = this.setterId;
            entity.status = this.status != null ? this.status : QuestionStatus.DRAFT;
            entity.version = this.version > 0 ? this.version : 1;
            entity.contentJson = this.contentJson;
            entity.hash = this.hash;
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
            status = QuestionStatus.DRAFT;
        }
        if (version <= 0) {
            version = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public UUID getPoolId() { return poolId; }
    public void setPoolId(UUID poolId) { this.poolId = poolId; }

    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }

    public int getUnit() { return unit; }
    public void setUnit(int unit) { this.unit = unit; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public CognitiveLevel getCognitiveLevel() { return cognitiveLevel; }
    public void setCognitiveLevel(CognitiveLevel cognitiveLevel) { this.cognitiveLevel = cognitiveLevel; }

    public String getSetterId() { return setterId; }
    public void setSetterId(String setterId) { this.setterId = setterId; }

    public QuestionStatus getStatus() { return status; }
    public void setStatus(QuestionStatus status) { this.status = status; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
