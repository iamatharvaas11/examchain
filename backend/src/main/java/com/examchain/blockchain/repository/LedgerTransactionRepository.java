package com.examchain.blockchain.repository;

import com.examchain.blockchain.entity.LedgerTransactionEntity;
import com.examchain.blockchain.model.LedgerTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransactionEntity, String> {
    List<LedgerTransactionEntity> findByEntityIdOrderByCreatedAtDesc(String entityId);
    List<LedgerTransactionEntity> findByStatus(LedgerTransactionStatus status);
    List<LedgerTransactionEntity> findAllByOrderByCreatedAtDesc();
}

