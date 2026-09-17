-- =============================================================================
-- EXAMCHAIN: Phase 1 Database Baseline
-- =============================================================================
-- Foundation baseline marker.
-- Domain tables are created in subsequent phases via Flyway migrations:
-- Phase 2: users, user_roles (Keycloak sync)
-- Phase 3: exams, subjects, questions, question_pools, blueprints
-- Phase 4+: papers, variants, centres, releases, incidents
-- =============================================================================

CREATE TABLE IF NOT EXISTS schema_baseline_info (
    installed_rank INTEGER NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    installed_on TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL,
    CONSTRAINT pk_schema_baseline PRIMARY KEY (installed_rank)
);

INSERT INTO schema_baseline_info (installed_rank, version, description, type, execution_time, success)
VALUES (1, '1', 'Phase 1 Foundation Baseline', 'SQL', 1, TRUE)
ON CONFLICT (installed_rank) DO NOTHING;
