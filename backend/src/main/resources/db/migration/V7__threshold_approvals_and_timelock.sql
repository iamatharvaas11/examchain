-- EXAMCHAIN Phase 9: Multi-Authority Threshold Approvals & Time-Lock Release

CREATE TABLE IF NOT EXISTS release_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paper_id VARCHAR(64) NOT NULL UNIQUE,
    exam_id UUID,
    required_threshold INT NOT NULL DEFAULT 2,
    current_approval_count INT NOT NULL DEFAULT 0,
    scheduled_release_time TIMESTAMP WITH TIME ZONE NOT NULL,
    release_window_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    released_at TIMESTAMP WITH TIME ZONE,
    released_by VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS paper_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paper_id VARCHAR(64) NOT NULL,
    authority_id VARCHAR(64) NOT NULL,
    authority_role VARCHAR(64) NOT NULL,
    authority_name VARCHAR(128),
    signature_hash VARCHAR(64) NOT NULL,
    comments VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_paper_authority UNIQUE (paper_id, authority_id)
);

CREATE INDEX IF NOT EXISTS idx_approvals_paper_id ON paper_approvals(paper_id);
CREATE INDEX IF NOT EXISTS idx_schedules_paper_id ON release_schedules(paper_id);

