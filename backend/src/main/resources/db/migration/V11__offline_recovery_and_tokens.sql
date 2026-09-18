-- EXAMCHAIN Phase 13: Offline Resilience, Single-Use Emergency Tokens & Reconnection Sync

CREATE TABLE IF NOT EXISTS offline_authorization_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_id VARCHAR(64) UNIQUE NOT NULL,
    centre_code VARCHAR(64) NOT NULL,
    paper_id VARCHAR(64) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    used_at TIMESTAMP WITH TIME ZONE,
    used_by_terminal VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS offline_audit_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    centre_code VARCHAR(64) NOT NULL,
    terminal_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload_json TEXT NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT false,
    synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_offline_token_hash ON offline_authorization_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_offline_queue_centre ON offline_audit_queue(centre_code);
CREATE INDEX IF NOT EXISTS idx_offline_queue_synced ON offline_audit_queue(synced);

