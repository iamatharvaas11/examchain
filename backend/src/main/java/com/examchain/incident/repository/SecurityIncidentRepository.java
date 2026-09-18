package com.examchain.incident.repository;

import com.examchain.incident.entity.SecurityIncidentEntity;
import com.examchain.incident.model.IncidentEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityIncidentRepository extends JpaRepository<SecurityIncidentEntity, UUID> {
    Optional<SecurityIncidentEntity> findByIncidentId(String incidentId);
    List<SecurityIncidentEntity> findByPaperId(String paperId);
    List<SecurityIncidentEntity> findByEventType(IncidentEventType eventType);
    List<SecurityIncidentEntity> findAllByOrderByCreatedAtDesc();
}

