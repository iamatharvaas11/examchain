package com.examchain.centre.service;

import com.examchain.centre.entity.CentreTerminalEntity;
import com.examchain.centre.repository.CentreTerminalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Evaluates browser and device fingerprint continuity as a risk-control signal.
 * Per Architecture Condition 2, this is a continuity & anomaly mechanism only,
 * not hardware cryptographic identity.
 */
@Service
public class DeviceFingerprintService {

    private static final Logger log = LoggerFactory.getLogger(DeviceFingerprintService.class);

    private final CentreTerminalRepository terminalRepository;

    @Autowired
    public DeviceFingerprintService(CentreTerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    public int evaluateRiskScore(String centreCode, String terminalId, String submittedFingerprint) {
        Optional<CentreTerminalEntity> terminalOpt = terminalRepository.findByCentreCodeAndTerminalId(centreCode, terminalId);

        if (terminalOpt.isEmpty()) {
            log.warn("Unknown terminal [{}] for centre [{}]. Elevated risk score assigned.", terminalId, centreCode);
            return 85; // Elevated risk for unregistered terminal
        }

        CentreTerminalEntity terminal = terminalOpt.get();

        if ("BLOCKED".equalsIgnoreCase(terminal.getStatus())) {
            log.warn("Terminal [{}] for centre [{}] is explicitly blocked.", terminalId, centreCode);
            return 100;
        }

        terminal.setLastSeenAt(Instant.now());
        terminalRepository.save(terminal);

        if (terminal.getRegisteredFingerprint().equals(submittedFingerprint)) {
            return 0; // Known trusted terminal
        } else {
            log.warn("Fingerprint mismatch for terminal [{}]. Expected: [{}], Received: [{}]",
                    terminalId, terminal.getRegisteredFingerprint(), submittedFingerprint);
            return 65; // Suspicious change in browser/device fingerprint
        }
    }
}

