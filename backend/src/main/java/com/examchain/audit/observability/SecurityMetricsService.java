package com.examchain.audit.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SecurityMetricsService {

    private final MeterRegistry meterRegistry;

    @Autowired
    public SecurityMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPrint(String centreCode, String status) {
        Counter.builder("examchain.prints.total")
                .description("Total number of secure print executions")
                .tag("centre", centreCode != null ? centreCode : "unknown")
                .tag("status", status != null ? status : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordAnomaly(String eventType, String severity) {
        Counter.builder("examchain.anomalies.total")
                .description("Total number of detected security anomaly events")
                .tag("eventType", eventType != null ? eventType : "unknown")
                .tag("severity", severity != null ? severity : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordAuthFailure(String reason) {
        Counter.builder("examchain.auth.failures.total")
                .description("Total number of authentication/authorization failures")
                .tag("reason", reason != null ? reason : "unspecified")
                .register(meterRegistry)
                .increment();
    }

    public void recordApprovalTime(long durationMillis) {
        Timer.builder("examchain.approvals.duration")
                .description("Time taken to reach threshold quorum approval")
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMillis));
    }

    public void recordReleaseLatency(long latencyMillis) {
        Timer.builder("examchain.release.latency")
                .description("Latency between scheduled time-lock release and actual execution")
                .register(meterRegistry)
                .record(Duration.ofMillis(latencyMillis));
    }

    public double getPrintCount(String centreCode, String status) {
        Counter counter = meterRegistry.find("examchain.prints.total")
                .tags("centre", centreCode, "status", status)
                .counter();
        return counter != null ? counter.count() : 0.0;
    }

    public double getAnomalyCount(String eventType, String severity) {
        Counter counter = meterRegistry.find("examchain.anomalies.total")
                .tags("eventType", eventType, "severity", severity)
                .counter();
        return counter != null ? counter.count() : 0.0;
    }

    public double getAuthFailureCount(String reason) {
        Counter counter = meterRegistry.find("examchain.auth.failures.total")
                .tags("reason", reason)
                .counter();
        return counter != null ? counter.count() : 0.0;
    }
}
