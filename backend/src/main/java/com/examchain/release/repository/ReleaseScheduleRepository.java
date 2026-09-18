package com.examchain.release.repository;

import com.examchain.release.entity.ReleaseScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReleaseScheduleRepository extends JpaRepository<ReleaseScheduleEntity, UUID> {
    Optional<ReleaseScheduleEntity> findByPaperId(String paperId);
}

