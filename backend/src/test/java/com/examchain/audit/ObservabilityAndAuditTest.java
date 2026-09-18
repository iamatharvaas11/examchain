package com.examchain.audit;

import com.examchain.audit.observability.SecurityMetricsService;
import com.examchain.audit.observability.StructuredSecurityLogger;
import com.examchain.core.controller.HealthController;
import com.examchain.core.dto.ApiResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ObservabilityAndAuditTest {

    private SimpleMeterRegistry meterRegistry;
    private SecurityMetricsService metricsService;
    private StructuredSecurityLogger securityLogger;
    private DataSource dataSource;
    private Connection connection;
    private HealthController healthController;

    @BeforeEach
    void setUp() throws SQLException {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new SecurityMetricsService(meterRegistry);
        securityLogger = new StructuredSecurityLogger();

        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        healthController = new HealthController(dataSource);
    }

    @Test
    @DisplayName("Micrometer Prometheus metric counters increment as domain actions occur")
    void testMetricsCounters_Increment() {
        metricsService.recordPrint("CENTRE-01", "SUCCESS");
        metricsService.recordPrint("CENTRE-01", "SUCCESS");
        metricsService.recordAnomaly("DUPLICATE_PRINT", "CRITICAL");
        metricsService.recordAuthFailure("BAD_TOKEN");
        metricsService.recordApprovalTime(250);
        metricsService.recordReleaseLatency(120);

        assertThat(metricsService.getPrintCount("CENTRE-01", "SUCCESS")).isEqualTo(2.0);
        assertThat(metricsService.getAnomalyCount("DUPLICATE_PRINT", "CRITICAL")).isEqualTo(1.0);
        assertThat(metricsService.getAuthFailureCount("BAD_TOKEN")).isEqualTo(1.0);

        assertThat(meterRegistry.find("examchain.approvals.duration").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.find("examchain.release.latency").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Structured security logger redacts Bearer tokens, passwords, and secret keys")
    void testStructuredSecurityLogger_RedactsSensitiveData() {
        String sensitivePayload = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.t-ID | \"password\":\"SuperSecret123\" | encryptionKey: 4f8b2c1d9e7a6f5e";

        String sanitized = securityLogger.sanitize(sensitivePayload);

        assertThat(sanitized).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        assertThat(sanitized).contains("Bearer [REDACTED_BY_AUDIT_POLICY]");

        assertThat(sanitized).doesNotContain("SuperSecret123");
        assertThat(sanitized).contains("[REDACTED_PASSWORD]");

        assertThat(sanitized).doesNotContain("4f8b2c1d9e7a6f5e");
        assertThat(sanitized).contains("encryptionKey=[REDACTED_SECRET]");
    }

    @Test
    @DisplayName("Extended health check verifies database, object storage, crypto enclave, and ledger")
    @SuppressWarnings("unchecked")
    void testExtendedHealthCheck_VerifiesAllSubsystems() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = healthController.getExtendedHealth();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = response.getBody().data();
        assertThat(body.get("overallStatus")).isEqualTo("UP");

        Map<String, String> subsystems = (Map<String, String>) body.get("subsystems");
        assertThat(subsystems.get("database")).isEqualTo("CONNECTED");
        assertThat(subsystems.get("cryptoEnclave")).isEqualTo("HEALTHY");
        assertThat(subsystems.get("objectStorage")).isEqualTo("HEALTHY_SECURE");
        assertThat(subsystems.get("ledgerConnectivity")).isEqualTo("CONNECTED_LOCAL_MIRROR");
    }
}

