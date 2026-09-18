package com.examchain.exam.dto;

import com.examchain.exam.model.AssignmentStatus;
import com.examchain.exam.model.ExamStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public class ExamDtos {

    public record ExamRequest(
            @NotBlank(message = "Exam code is required") String examCode,
            @NotBlank(message = "Title is required") String title,
            String description,
            @NotBlank(message = "Academic session is required") String academicSession,
            ExamStatus status
    ) {}

    public record ExamResponse(
            UUID id,
            String examCode,
            String title,
            String description,
            String academicSession,
            ExamStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record SubjectRequest(
            @NotBlank(message = "Subject code is required") String subjectCode,
            @NotBlank(message = "Subject name is required") String name,
            @Positive(message = "Total marks must be positive") int totalMarks,
            @Positive(message = "Passing marks must be positive") int passingMarks
    ) {}

    public record SubjectResponse(
            UUID id,
            UUID examId,
            String subjectCode,
            String name,
            int totalMarks,
            int passingMarks,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record AssignSetterRequest(
            @NotBlank(message = "Setter ID is required") String setterId
    ) {}

    public record SetterAssignmentResponse(
            UUID id,
            UUID subjectId,
            String setterId,
            String assignedBy,
            AssignmentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}

