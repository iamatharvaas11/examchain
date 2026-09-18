package com.examchain.incident.repository;

import com.examchain.incident.entity.SystemFreezeStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemFreezeStateRepository extends JpaRepository<SystemFreezeStateEntity, Integer> {
}

