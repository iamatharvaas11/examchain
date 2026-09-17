package com.examchain.core.controller;

import com.examchain.core.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check controller providing system status and database connectivity details.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final DataSource dataSource;

    @Value("${spring.application.name:examchain}")
    private String applicationName;

    @Value("${examchain.version:0.1.0}")
    private String version;

    private final Instant startTime = Instant.now();

    @Autowired
    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("application", applicationName);
        status.put("version", version);
        status.put("status", "UP");
        status.put("startedAt", startTime.toString());

        String dbStatus = "DISCONNECTED";
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                dbStatus = "CONNECTED";
            }
        } catch (SQLException e) {
            dbStatus = "ERROR: " + e.getMessage();
        }

        status.put("database", dbStatus);
        return ResponseEntity.ok(ApiResponse.success("System operational", status));
    }
}
