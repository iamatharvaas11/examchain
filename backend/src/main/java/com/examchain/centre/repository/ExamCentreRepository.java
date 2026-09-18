package com.examchain.centre.repository;

import com.examchain.centre.entity.ExamCentreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamCentreRepository extends JpaRepository<ExamCentreEntity, UUID> {
    Optional<ExamCentreEntity> findByCentreCode(String centreCode);
}

