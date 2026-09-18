package com.examchain.offline.repository;

import com.examchain.offline.entity.OfflineTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfflineTokenRepository extends JpaRepository<OfflineTokenEntity, UUID> {
    Optional<OfflineTokenEntity> findByTokenHash(String tokenHash);
    Optional<OfflineTokenEntity> findByTokenId(String tokenId);
}
