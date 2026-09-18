package com.examchain.incident.service;

import com.examchain.incident.dto.IncidentDtos.FreezeStateResponse;
import com.examchain.incident.entity.SystemFreezeStateEntity;
import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.repository.SystemFreezeStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class FreezeModeService {

    private static final Logger log = LoggerFactory.getLogger(FreezeModeService.class);

    private final SystemFreezeStateRepository freezeRepository;

    @Autowired
    public FreezeModeService(SystemFreezeStateRepository freezeRepository) {
        this.freezeRepository = freezeRepository;
    }

    @Transactional(readOnly = true)
    public FreezeLevel getCurrentLevel() {
        return freezeRepository.findById(1)
                .map(SystemFreezeStateEntity::getFreezeLevel)
                .orElse(FreezeLevel.NORMAL);
    }

    @Transactional(readOnly = true)
    public FreezeStateResponse getFreezeState() {
        SystemFreezeStateEntity entity = freezeRepository.findById(1)
                .orElseGet(() -> new SystemFreezeStateEntity(FreezeLevel.NORMAL, "Default", "SYSTEM"));
        return new FreezeStateResponse(entity.getFreezeLevel(), entity.getReason(), entity.getTriggeredBy(), entity.getUpdatedAt());
    }

    @Transactional
    public FreezeStateResponse updateFreezeLevel(FreezeLevel newLevel, String reason, String actor) {
        SystemFreezeStateEntity entity = freezeRepository.findById(1)
                .orElseGet(SystemFreezeStateEntity::new);

        FreezeLevel previousLevel = entity.getFreezeLevel();
        entity.setFreezeLevel(newLevel);
        entity.setReason(reason);
        entity.setTriggeredBy(actor);
        entity.setUpdatedAt(Instant.now());

        SystemFreezeStateEntity saved = freezeRepository.save(entity);
        log.warn("SYSTEM FREEZE LEVEL CHANGED: [{}] -> [{}] by [{}] (Reason: {})",
                previousLevel, newLevel, actor, reason);

        return new FreezeStateResponse(saved.getFreezeLevel(), saved.getReason(), saved.getTriggeredBy(), saved.getUpdatedAt());
    }

    public void assertNotFrozen() {
        if (getCurrentLevel() == FreezeLevel.FROZEN) {
            throw new IllegalStateException("Security alert: examination system is in FROZEN mode. All print and release actions are halted.");
        }
    }
}

