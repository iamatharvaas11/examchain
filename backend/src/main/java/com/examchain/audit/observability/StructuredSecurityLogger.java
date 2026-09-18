package com.examchain.audit.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.regex.Pattern;

@Component
public class StructuredSecurityLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");

    private static final Pattern BEARER_PATTERN = Pattern.compile("Bearer\\s+[A-Za-z0-9\\-\\._~\\+\\/]+=*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")");
    private static final Pattern SECRET_KEY_PATTERN = Pattern.compile("(?i)(secret|apiKey|encryptionKey|privateKey|tokenSecret)\\s*[:=]\\s*([\"']?[A-Za-z0-9+/=_-]{8,}[\"']?)");

    public String sanitize(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = BEARER_PATTERN.matcher(message).replaceAll("Bearer [REDACTED_BY_AUDIT_POLICY]");
        sanitized = PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1[REDACTED_PASSWORD]$2");
        sanitized = SECRET_KEY_PATTERN.matcher(sanitized).replaceAll("$1=[REDACTED_SECRET]");
        return sanitized;
    }

    public void logSecurityEvent(String eventType, String actor, String resource, String action, String outcome, String rawDetails) {
        String cleanDetails = sanitize(rawDetails);
        String structuredJson = String.format(
                "{\"timestamp\":\"%s\",\"eventType\":\"%s\",\"actor\":\"%s\",\"resource\":\"%s\",\"action\":\"%s\",\"outcome\":\"%s\",\"details\":\"%s\"}",
                Instant.now(), eventType, actor, resource, action, outcome, cleanDetails
        );
        log.info("{}", structuredJson);
    }
}

