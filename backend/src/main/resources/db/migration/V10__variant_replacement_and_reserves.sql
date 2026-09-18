-- EXAMCHAIN Phase 12: Variant Containment & Replacement Lineage

CREATE TABLE IF NOT EXISTS variant_replacements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    replacement_id VARCHAR(64) UNIQUE NOT NULL,
    quarantined_paper_id VARCHAR(64) NOT NULL,
    reserve_paper_id VARCHAR(64) NOT NULL,
    exam_code VARCHAR(64) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    authorized_by VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rpl_quarantined ON variant_replacements(quarantined_paper_id);
CREATE INDEX IF NOT EXISTS idx_rpl_reserve ON variant_replacements(reserve_paper_id);

