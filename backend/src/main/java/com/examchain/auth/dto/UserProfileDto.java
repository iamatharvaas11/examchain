package com.examchain.auth.dto;

import java.util.Set;

/**
 * Current authenticated user profile extracted from validated token claims.
 */
public record UserProfileDto(
        String userId,
        String username,
        String email,
        String displayName,
        Set<String> roles
) {
}

