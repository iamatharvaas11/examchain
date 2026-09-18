package com.examchain.auth.model;

/**
 * Canonical user roles for EXAMCHAIN with strict least-privilege boundaries.
 */
public enum UserRole {
    SUPER_ADMIN,
    EXAM_AUTHORITY,
    CONTROLLER,
    PAPER_SETTER,
    CENTRE_ADMIN,
    EXAM_OPERATOR,
    AUDITOR,
    STUDENT;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}

