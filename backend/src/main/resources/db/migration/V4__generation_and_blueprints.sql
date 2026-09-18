-- EXAMCHAIN Phase 5: Paper Blueprints & Dynamic Paper Generation Schema

CREATE TABLE IF NOT EXISTS blueprints (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    blueprint_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    total_marks INT NOT NULL,
    duration_minutes INT NOT NULL,
    policy_mode VARCHAR(50) NOT NULL,
    rules_json TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blueprints_subject ON blueprints(subject_id);
CREATE INDEX IF NOT EXISTS idx_blueprints_code ON blueprints(blueprint_code);

CREATE TABLE IF NOT EXISTS generated_papers (
    id UUID PRIMARY KEY,
    paper_id VARCHAR(50) NOT NULL UNIQUE,
    blueprint_id UUID NOT NULL REFERENCES blueprints(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    set_code VARCHAR(50) NOT NULL,
    total_marks INT NOT NULL,
    question_count INT NOT NULL,
    paper_hash VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'GENERATED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_papers_blueprint ON generated_papers(blueprint_id);
CREATE INDEX IF NOT EXISTS idx_papers_business_id ON generated_papers(paper_id);

CREATE TABLE IF NOT EXISTS generated_paper_questions (
    id UUID PRIMARY KEY,
    paper_id UUID NOT NULL REFERENCES generated_papers(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    sequence_number INT NOT NULL,
    allocated_marks INT NOT NULL,
    CONSTRAINT uk_paper_question UNIQUE (paper_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_paper_questions_paper ON generated_paper_questions(paper_id);

