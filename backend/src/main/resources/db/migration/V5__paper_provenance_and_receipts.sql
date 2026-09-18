-- EXAMCHAIN Phase 7: Paper Provenance, QR Mapping & Verification Receipts

CREATE TABLE IF NOT EXISTS paper_provenance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trace_id VARCHAR(64) NOT NULL UNIQUE,
    paper_id VARCHAR(64) NOT NULL,
    paper_hash VARCHAR(64) NOT NULL,
    exam_code VARCHAR(64) NOT NULL,
    exam_title VARCHAR(255) NOT NULL,
    centre_code VARCHAR(64),
    exam_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    exam_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'LOCKED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_provenance_trace_id ON paper_provenance(trace_id);
CREATE INDEX IF NOT EXISTS idx_provenance_paper_id ON paper_provenance(paper_id);

