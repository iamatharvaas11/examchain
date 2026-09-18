package com.examchain.offline.service;

import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.offline.dto.OfflineDtos.*;
import com.examchain.offline.entity.OfflineTokenEntity;
import com.examchain.offline.repository.OfflineTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class OfflineTokenService {

    private static final Logger log = LoggerFactory.getLogger(OfflineTokenService.class);

    private final OfflineTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public OfflineTokenService(OfflineTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public GeneratedTokenResponse generateOfflineToken(GenerateOfflineTokenRequest request) {
        byte[] tokenBytes = new byte[16];
        secureRandom.nextBytes(tokenBytes);
        String rawTokenSecret = "OFL-SEC-" + HexFormat.of().formatHex(tokenBytes).toUpperCase();
        String tokenHash = hashToken(rawTokenSecret);

        byte[] idBytes = new byte[4];
        secureRandom.nextBytes(idBytes);
        String tokenId = "OFL-TKN-" + HexFormat.of().formatHex(idBytes).toUpperCase();

        OfflineTokenEntity entity = new OfflineTokenEntity(
                tokenId,
                request.centreCode(),
                request.paperId(),
                tokenHash,
                request.validFrom(),
                request.validUntil()
        );
        tokenRepository.save(entity);

        log.info("Generated offline emergency authorization token [{}] for centre [{}] and paper [{}]",
                tokenId, request.centreCode(), request.paperId());

        return new GeneratedTokenResponse(
                tokenId,
                rawTokenSecret,
                request.centreCode(),
                request.paperId(),
                request.validFrom(),
                request.validUntil()
        );
    }

    @Transactional
    public TokenValidationResponse validateAndConsumeToken(ConsumeTokenRequest request) {
        String tokenHash = hashToken(request.rawTokenSecret());
        OfflineTokenEntity entity = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unrecognized offline token."));

        if (!entity.getCentreCode().equals(request.centreCode()) || !entity.getPaperId().equals(request.paperId())) {
            throw new IllegalArgumentException("Token parameters mismatch: token was not issued for this centre/paper.");
        }

        // Anti-replay check
        if (entity.isUsed()) {
            log.error("OFFLINE TOKEN REPLAY DETECTED on token [{}] at centre [{}]", entity.getTokenId(), request.centreCode());
            throw new IllegalStateException("Offline token replay detected: token has already been consumed at " + entity.getUsedAt());
        }

        // Time-lock validity check
        Instant now = Instant.now();
        if (now.isBefore(entity.getValidFrom()) || now.isAfter(entity.getValidUntil())) {
            throw new IllegalStateException("Offline token is expired or outside the permitted emergency release window.");
        }

        entity.setUsed(true);
        entity.setUsedAt(now);
        entity.setUsedByTerminal(request.terminalId());
        tokenRepository.save(entity);

        log.info("Successfully validated and consumed emergency offline token [{}] for terminal [{}]",
                entity.getTokenId(), request.terminalId());

        return new TokenValidationResponse(
                true,
                entity.getTokenId(),
                entity.getCentreCode(),
                entity.getPaperId(),
                "Offline token verified and consumed. Single-use emergency decryption permitted.",
                now
        );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

