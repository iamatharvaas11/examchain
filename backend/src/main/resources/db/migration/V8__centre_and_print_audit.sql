-- EXAMCHAIN Phase 10: Centre Terminals, Secure In-Memory Printing & Print Receipts

CREATE TABLE IF NOT EXISTS exam_centres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    centre_code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    city VARCHAR(64) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS centre_terminals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    centre_code VARCHAR(64) NOT NULL,
    terminal_id VARCHAR(64) NOT NULL,
    registered_fingerprint VARCHAR(128) NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'TRUSTED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_centre_terminal UNIQUE (centre_code, terminal_id)
);

CREATE TABLE IF NOT EXISTS print_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paper_id VARCHAR(64) NOT NULL,
    centre_code VARCHAR(64) NOT NULL,
    terminal_id VARCHAR(64) NOT NULL,
    operator_id VARCHAR(64) NOT NULL,
    device_fingerprint VARCHAR(128) NOT NULL,
    risk_score INT NOT NULL,
    watermark_text VARCHAR(255) NOT NULL,
    copy_count INT NOT NULL,
    receipt_id VARCHAR(64) UNIQUE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_print_paper_id ON print_audit_logs(paper_id);
CREATE INDEX IF NOT EXISTS idx_print_centre ON print_audit_logs(centre_code);
CREATE INDEX IF NOT EXISTS idx_print_receipt_id ON print_audit_logs(receipt_id);

