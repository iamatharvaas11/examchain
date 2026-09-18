package com.examchain.auth;

import com.examchain.auth.config.KeycloakJwtConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakJwtConverterTest {

    private final KeycloakJwtConverter converter = new KeycloakJwtConverter();

    @Test
    @DisplayName("Extracts realm_access roles and maps them to ROLE_<ROLE_NAME>")
    void shouldExtractRealmRoles() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject("user-uuid-999")
                .claim("preferred_username", "bob_controller")
                .claim("realm_access", Map.of("roles", List.of("CONTROLLER", "EXAM_AUTHORITY", "offline_access")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token).isNotNull();
        assertThat(token.getName()).isEqualTo("bob_controller");

        Set<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_CONTROLLER", "ROLE_EXAM_AUTHORITY", "ROLE_OFFLINE_ACCESS");
    }

    @Test
    @DisplayName("Extracts resource_access client roles and maps them to ROLE_<ROLE_NAME>")
    void shouldExtractResourceAccessRoles() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject("user-uuid-888")
                .claim("preferred_username", "sam_setter")
                .claim("resource_access", Map.of("examchain-app", Map.of("roles", List.of("PAPER_SETTER"))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token).isNotNull();
        Set<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_PAPER_SETTER");
    }

    @Test
    @DisplayName("Falls back to subject if preferred_username is missing")
    void shouldFallbackToSubjectWhenUsernameMissing() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject("sub-fallback-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token).isNotNull();
        assertThat(token.getName()).isEqualTo("sub-fallback-123");
    }
}
