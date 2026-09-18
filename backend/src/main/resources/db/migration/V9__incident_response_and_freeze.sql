-- EXAMCHAIN Phase 11: Incident Response, Anomaly Events & System Freeze

CREATE TABLE IF NOT EXISTS system_freeze_state (
    id INT PRIMARY KEY DEFAULT 1,
    freeze_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    reason VARCHAR(255),
    triggered_by VARCHAR(64),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT single_freeze_row CHECK (id = 1)
);

INSERT INTO system_freeze_state (id, freeze_level, reason, triggered_by, updated_at)
VALUES (1, 'NORMAL', 'System initialization', 'SYSTEM', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS security_incident_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id VARCHAR(64) UNIQUE NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    paper_id VARCHAR(64),
    centre_code VARCHAR(64),
    operator_id VARCHAR(64),
    details TEXT NOT NULL,
    is_quarantined BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incident_paper_id ON security_incident_events(paper_id);
CREATE INDEX IF NOT EXISTS idx_incident_type ON security_incident_events(event_type);
CREATE INDEX IF NOT EXISTS idx_incident_severity ON security_incident_events(severity);

