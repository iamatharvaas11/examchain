package com.examchain.generation.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
    name = "generated_paper_questions",
    uniqueConstraints = @UniqueConstraint(name = "uk_paper_question", columnNames = {"paper_id", "question_id"})
)
public class GeneratedPaperQuestionEntity {

    @Id
    private UUID id;

    @Column(name = "paper_id", nullable = false)
    private UUID paperId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "allocated_marks", nullable = false)
    private int allocatedMarks;

    public GeneratedPaperQuestionEntity() {}

    public GeneratedPaperQuestionEntity(UUID id, UUID paperId, UUID questionId, int sequenceNumber, int allocatedMarks) {
        this.id = id;
        this.paperId = paperId;
        this.questionId = questionId;
        this.sequenceNumber = sequenceNumber;
        this.allocatedMarks = allocatedMarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID paperId;
        private UUID questionId;
        private int sequenceNumber;
        private int allocatedMarks;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder paperId(UUID paperId) { this.paperId = paperId; return this; }
        public Builder questionId(UUID questionId) { this.questionId = questionId; return this; }
        public Builder sequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; return this; }
        public Builder allocatedMarks(int allocatedMarks) { this.allocatedMarks = allocatedMarks; return this; }

        public GeneratedPaperQuestionEntity build() {
            GeneratedPaperQuestionEntity entity = new GeneratedPaperQuestionEntity();
            entity.id = this.id;
            entity.paperId = this.paperId;
            entity.questionId = this.questionId;
            entity.sequenceNumber = this.sequenceNumber;
            entity.allocatedMarks = this.allocatedMarks;
            return entity;
        }
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPaperId() { return paperId; }
    public void setPaperId(UUID paperId) { this.paperId = paperId; }

    public UUID getQuestionId() { return questionId; }
    public void setQuestionId(UUID questionId) { this.questionId = questionId; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public int getAllocatedMarks() { return allocatedMarks; }
    public void setAllocatedMarks(int allocatedMarks) { this.allocatedMarks = allocatedMarks; }
}
