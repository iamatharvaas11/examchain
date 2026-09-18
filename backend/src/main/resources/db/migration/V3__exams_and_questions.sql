-- EXAMCHAIN Phase 3: Exam, Subject, Setter Assignment, Question Pool & Question Schema

CREATE TABLE IF NOT EXISTS exams (
    id UUID PRIMARY KEY,
    exam_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    academic_session VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_exams_code ON exams(exam_code);

CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    subject_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    total_marks INT NOT NULL,
    passing_marks INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_exam_subject UNIQUE (exam_id, subject_code)
);

CREATE INDEX IF NOT EXISTS idx_subjects_exam ON subjects(exam_id);

CREATE TABLE IF NOT EXISTS setter_assignments (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    setter_id VARCHAR(100) NOT NULL,
    assigned_by VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_subject_setter UNIQUE (subject_id, setter_id)
);

CREATE INDEX IF NOT EXISTS idx_assignments_setter ON setter_assignments(setter_id);

CREATE TABLE IF NOT EXISTS question_pools (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    pool_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_subject_pool UNIQUE (subject_id, pool_code)
);

CREATE INDEX IF NOT EXISTS idx_pools_subject ON question_pools(subject_id);

CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY,
    question_id VARCHAR(50) NOT NULL UNIQUE,
    pool_id UUID NOT NULL REFERENCES question_pools(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    unit INT NOT NULL,
    marks INT NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    cognitive_level VARCHAR(30) NOT NULL,
    setter_id VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    content_json TEXT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_questions_pool ON questions(pool_id);
CREATE INDEX IF NOT EXISTS idx_questions_subject ON questions(subject_id);
CREATE INDEX IF NOT EXISTS idx_questions_business_id ON questions(question_id);
