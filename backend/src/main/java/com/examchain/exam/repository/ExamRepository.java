package com.examchain.exam.repository;

import com.examchain.exam.entity.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, UUID> {
    Optional<ExamEntity> findByExamCode(String examCode);
    boolean existsByExamCode(String examCode);
}

