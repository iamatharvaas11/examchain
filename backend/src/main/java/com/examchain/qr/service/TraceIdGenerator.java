package com.examchain.qr.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
public class TraceIdGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateTraceId() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return "TRC-" + HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
