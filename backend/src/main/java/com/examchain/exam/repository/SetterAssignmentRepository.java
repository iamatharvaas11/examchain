package com.examchain.exam.repository;

import com.examchain.exam.entity.SetterAssignmentEntity;
import com.examchain.exam.model.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SetterAssignmentRepository extends JpaRepository<SetterAssignmentEntity, UUID> {
    List<SetterAssignmentEntity> findBySubjectId(UUID subjectId);
    List<SetterAssignmentEntity> findBySetterId(String setterId);
    Optional<SetterAssignmentEntity> findBySubjectIdAndSetterId(UUID subjectId, String setterId);
    boolean existsBySubjectIdAndSetterIdAndStatus(UUID subjectId, String setterId, AssignmentStatus status);
}

