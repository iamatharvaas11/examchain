package com.examchain.exam.controller;

import com.examchain.core.dto.ApiResponse;
import com.examchain.exam.dto.ExamDtos.*;
import com.examchain.exam.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authority")
public class ExamController {

    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@Valid @RequestBody ExamRequest request) {
        ExamResponse response = examService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Exam created successfully", response));
    }

    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams() {
        List<ExamResponse> response = examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.success("Exams retrieved", response));
    }

    @GetMapping("/exams/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable UUID id) {
        ExamResponse response = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.success("Exam retrieved", response));
    }

    @PostMapping("/exams/{examId}/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @PathVariable UUID examId,
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse response = examService.createSubject(examId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Subject created successfully", response));
    }

    @GetMapping("/exams/{examId}/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsForExam(@PathVariable UUID examId) {
        List<SubjectResponse> response = examService.getSubjectsForExam(examId);
        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved", response));
    }

    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable UUID subjectId) {
        SubjectResponse response = examService.getSubjectById(subjectId);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved", response));
    }

    @PostMapping("/subjects/{subjectId}/setters")
    public ResponseEntity<ApiResponse<SetterAssignmentResponse>> assignSetter(
            @PathVariable UUID subjectId,
            @Valid @RequestBody AssignSetterRequest request,
            Authentication authentication
    ) {
        String assignedBy = authentication != null ? authentication.getName() : "system";
        SetterAssignmentResponse response = examService.assignSetter(subjectId, request.setterId(), assignedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Setter assigned successfully", response));
    }

    @GetMapping("/subjects/{subjectId}/setters")
    public ResponseEntity<ApiResponse<List<SetterAssignmentResponse>>> getAssignmentsForSubject(@PathVariable UUID subjectId) {
        List<SetterAssignmentResponse> response = examService.getAssignmentsForSubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success("Assignments retrieved", response));
    }
}

