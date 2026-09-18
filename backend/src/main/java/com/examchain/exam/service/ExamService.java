package com.examchain.exam.service;

import com.examchain.exam.dto.ExamDtos.*;
import com.examchain.exam.entity.ExamEntity;
import com.examchain.exam.entity.SetterAssignmentEntity;
import com.examchain.exam.entity.SubjectEntity;
import com.examchain.exam.model.AssignmentStatus;
import com.examchain.exam.model.ExamStatus;
import com.examchain.exam.repository.ExamRepository;
import com.examchain.exam.repository.SetterAssignmentRepository;
import com.examchain.exam.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final SetterAssignmentRepository setterAssignmentRepository;

    @Autowired
    public ExamService(
            ExamRepository examRepository,
            SubjectRepository subjectRepository,
            SetterAssignmentRepository setterAssignmentRepository
    ) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.setterAssignmentRepository = setterAssignmentRepository;
    }

    public ExamResponse createExam(ExamRequest request) {
        if (examRepository.existsByExamCode(request.examCode())) {
            throw new IllegalArgumentException("Exam with code '" + request.examCode() + "' already exists");
        }

        ExamEntity entity = ExamEntity.builder()
                .examCode(request.examCode().trim().toUpperCase())
                .title(request.title().trim())
                .description(request.description())
                .academicSession(request.academicSession().trim())
                .status(request.status() != null ? request.status() : ExamStatus.DRAFT)
                .build();

        ExamEntity saved = examRepository.save(entity);
        return mapToExamResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExamResponse getExamById(UUID id) {
        ExamEntity entity = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        return mapToExamResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::mapToExamResponse)
                .toList();
    }

    public SubjectResponse createSubject(UUID examId, SubjectRequest request) {
        if (!examRepository.existsById(examId)) {
            throw new IllegalArgumentException("Exam not found: " + examId);
        }
        if (subjectRepository.existsByExamIdAndSubjectCode(examId, request.subjectCode())) {
            throw new IllegalArgumentException("Subject code '" + request.subjectCode() + "' already exists in exam " + examId);
        }
        if (request.passingMarks() > request.totalMarks()) {
            throw new IllegalArgumentException("Passing marks cannot exceed total marks");
        }

        SubjectEntity entity = SubjectEntity.builder()
                .examId(examId)
                .subjectCode(request.subjectCode().trim().toUpperCase())
                .name(request.name().trim())
                .totalMarks(request.totalMarks())
                .passingMarks(request.passingMarks())
                .build();

        SubjectEntity saved = subjectRepository.save(entity);
        return mapToSubjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID subjectId) {
        SubjectEntity entity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        return mapToSubjectResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsForExam(UUID examId) {
        return subjectRepository.findByExamId(examId).stream()
                .map(this::mapToSubjectResponse)
                .toList();
    }

    public SetterAssignmentResponse assignSetter(UUID subjectId, String setterId, String assignedBy) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Subject not found: " + subjectId);
        }

        SetterAssignmentEntity assignment = setterAssignmentRepository
                .findBySubjectIdAndSetterId(subjectId, setterId)
                .orElse(null);

        if (assignment != null) {
            assignment.setStatus(AssignmentStatus.ACTIVE);
            assignment.setAssignedBy(assignedBy);
        } else {
            assignment = SetterAssignmentEntity.builder()
                    .subjectId(subjectId)
                    .setterId(setterId.trim())
                    .assignedBy(assignedBy)
                    .status(AssignmentStatus.ACTIVE)
                    .build();
        }

        SetterAssignmentEntity saved = setterAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SetterAssignmentResponse> getAssignmentsForSubject(UUID subjectId) {
        return setterAssignmentRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SetterAssignmentResponse> getAssignmentsForSetter(String setterId) {
        return setterAssignmentRepository.findBySetterId(setterId).stream()
                .map(this::mapToAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isSetterAssignedToSubject(UUID subjectId, String setterId) {
        return setterAssignmentRepository.existsBySubjectIdAndSetterIdAndStatus(subjectId, setterId, AssignmentStatus.ACTIVE);
    }

    private ExamResponse mapToExamResponse(ExamEntity e) {
        return new ExamResponse(
                e.getId(),
                e.getExamCode(),
                e.getTitle(),
                e.getDescription(),
                e.getAcademicSession(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private SubjectResponse mapToSubjectResponse(SubjectEntity s) {
        return new SubjectResponse(
                s.getId(),
                s.getExamId(),
                s.getSubjectCode(),
                s.getName(),
                s.getTotalMarks(),
                s.getPassingMarks(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private SetterAssignmentResponse mapToAssignmentResponse(SetterAssignmentEntity a) {
        return new SetterAssignmentResponse(
                a.getId(),
                a.getSubjectId(),
                a.getSetterId(),
                a.getAssignedBy(),
                a.getStatus(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}

