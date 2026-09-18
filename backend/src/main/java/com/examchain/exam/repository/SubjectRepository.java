package com.examchain.exam.repository;

import com.examchain.exam.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID> {
    List<SubjectEntity> findByExamId(UUID examId);
    Optional<SubjectEntity> findByExamIdAndSubjectCode(UUID examId, String subjectCode);
    boolean existsByExamIdAndSubjectCode(UUID examId, String subjectCode);
}
