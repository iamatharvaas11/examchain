package com.examchain.auth.controller;

import com.examchain.auth.dto.UserProfileDto;
import com.examchain.core.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for authenticated user profile queries and role-boundary validation probes.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String userId = authentication.getName();
        String username = authentication.getName();
        String email = "";
        String displayName = authentication.getName();

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getSubject();
            if (jwt.hasClaim("preferred_username")) {
                username = jwt.getClaimAsString("preferred_username");
            }
            if (jwt.hasClaim("email")) {
                email = jwt.getClaimAsString("email");
            }
            if (jwt.hasClaim("name")) {
                displayName = jwt.getClaimAsString("name");
            }
        }

        UserProfileDto profile = new UserProfileDto(userId, username, email, displayName, roles);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", profile));
    }

    // Role-boundary validation endpoints
    @GetMapping("/admin/ping")
    public ResponseEntity<ApiResponse<String>> adminPing() {
        return ResponseEntity.ok(ApiResponse.success("Admin authorized", "SUPER_ADMIN access confirmed"));
    }

    @GetMapping("/authority/ping")
    public ResponseEntity<ApiResponse<String>> authorityPing() {
        return ResponseEntity.ok(ApiResponse.success("Authority authorized", "EXAM_AUTHORITY / CONTROLLER access confirmed"));
    }

    @GetMapping("/setter/ping")
    public ResponseEntity<ApiResponse<String>> setterPing() {
        return ResponseEntity.ok(ApiResponse.success("Setter authorized", "PAPER_SETTER access confirmed"));
    }

    @GetMapping("/centre/ping")
    public ResponseEntity<ApiResponse<String>> centrePing() {
        return ResponseEntity.ok(ApiResponse.success("Centre authorized", "CENTRE_ADMIN / EXAM_OPERATOR access confirmed"));
    }

    @GetMapping("/operator/ping")
    public ResponseEntity<ApiResponse<String>> operatorPing() {
        return ResponseEntity.ok(ApiResponse.success("Operator authorized", "EXAM_OPERATOR access confirmed"));
    }

    @GetMapping("/audit/ping")
    public ResponseEntity<ApiResponse<String>> auditPing() {
        return ResponseEntity.ok(ApiResponse.success("Auditor authorized", "AUDITOR access confirmed"));
    }

    @GetMapping("/student/ping")
    public ResponseEntity<ApiResponse<String>> studentPing() {
        return ResponseEntity.ok(ApiResponse.success("Student authorized", "STUDENT access confirmed"));
    }
}

