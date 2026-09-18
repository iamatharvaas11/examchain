package com.examchain.centre.repository;

import com.examchain.centre.entity.CentreTerminalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CentreTerminalRepository extends JpaRepository<CentreTerminalEntity, UUID> {
    Optional<CentreTerminalEntity> findByCentreCodeAndTerminalId(String centreCode, String terminalId);
}

