package com.examchain.release.repository;

import com.examchain.release.entity.PaperApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperApprovalRepository extends JpaRepository<PaperApprovalEntity, UUID> {
    List<PaperApprovalEntity> findByPaperId(String paperId);
    Optional<PaperApprovalEntity> findByPaperIdAndAuthorityId(String paperId, String authorityId);
}

