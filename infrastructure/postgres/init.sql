-- =============================================================================
-- EXAMCHAIN — PostgreSQL Initialization
-- =============================================================================
-- This script runs once when the PostgreSQL container is first created.
-- It sets up extensions and baseline configuration.
-- Domain tables are managed by Flyway migrations in the backend.
-- =============================================================================

-- Enable useful extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'EXAMCHAIN PostgreSQL initialized successfully';
END $$;

