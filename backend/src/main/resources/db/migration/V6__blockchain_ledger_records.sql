-- EXAMCHAIN Phase 8: Blockchain Ledger Transactions and Local Mirror

CREATE TABLE IF NOT EXISTS ledger_transactions (
    tx_id VARCHAR(64) PRIMARY KEY,
    channel_id VARCHAR(32) NOT NULL DEFAULT 'examchain-channel',
    chaincode_id VARCHAR(32) NOT NULL DEFAULT 'examchain-cc',
    transaction_name VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL, -- COMMITTED, PENDING_SYNC, FALLBACK_QUEUED
    block_number BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_tx_name ON ledger_transactions(transaction_name);
CREATE INDEX IF NOT EXISTS idx_ledger_entity_id ON ledger_transactions(entity_id);
CREATE INDEX IF NOT EXISTS idx_ledger_status ON ledger_transactions(status);
