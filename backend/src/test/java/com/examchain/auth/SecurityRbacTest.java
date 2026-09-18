package com.examchain.auth;

import com.examchain.ExamchainApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamchainApplication.class)
@ActiveProfiles("test")
public class SecurityRbacTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Public endpoint /api/v1/health allows unauthenticated access")
    void shouldAllowPublicHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthenticated request to /api/v1/auth/me returns 401 Unauthorized with ErrorResponse")
    void shouldRejectUnauthenticatedAccessToMe() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"));
    }

    @Test
    @DisplayName("Unauthenticated request to protected probe returns 401 Unauthorized")
    void shouldRejectUnauthenticatedAccessToProtectedProbe() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Valid JWT retrieves user profile from /api/v1/auth/me")
    void shouldAllowAuthenticatedUserToGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(builder -> builder
                                .subject("usr-12345")
                                .claim("preferred_username", "alice_authority")
                                .claim("email", "alice@examchain.org")
                                .claim("name", "Alice Examiner"))
                                .authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("usr-12345"))
                .andExpect(jsonPath("$.data.username").value("alice_authority"))
                .andExpect(jsonPath("$.data.email").value("alice@examchain.org"))
                .andExpect(jsonPath("$.data.displayName").value("Alice Examiner"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_EXAM_AUTHORITY"));
    }

    @Test
    @DisplayName("SUPER_ADMIN role accesses /api/v1/admin/ping successfully")
    void shouldAllowSuperAdminAccessToAdminPing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("EXAM_AUTHORITY and CONTROLLER access /api/v1/authority/ping successfully")
    void shouldAllowAuthorityAndControllerAccessToAuthorityPing() throws Exception {
        mockMvc.perform(get("/api/v1/authority/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/authority/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CONTROLLER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PAPER_SETTER accesses /api/v1/setter/ping successfully")
    void shouldAllowPaperSetterAccessToSetterPing() throws Exception {
        mockMvc.perform(get("/api/v1/setter/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("CENTRE_ADMIN and EXAM_OPERATOR access /api/v1/centre/ping successfully")
    void shouldAllowCentreAdminAndOperatorAccessToCentrePing() throws Exception {
        mockMvc.perform(get("/api/v1/centre/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CENTRE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/centre/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_OPERATOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("EXAM_OPERATOR accesses /api/v1/operator/ping successfully")
    void shouldAllowOperatorAccessToOperatorPing() throws Exception {
        mockMvc.perform(get("/api/v1/operator/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_OPERATOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("AUDITOR and SUPER_ADMIN access /api/v1/audit/ping successfully")
    void shouldAllowAuditorAndAdminAccessToAuditPing() throws Exception {
        mockMvc.perform(get("/api/v1/audit/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUDITOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/audit/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("STUDENT accesses /api/v1/student/ping successfully")
    void shouldAllowStudentAccessToStudentPing() throws Exception {
        mockMvc.perform(get("/api/v1/student/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("STUDENT role receives 403 Forbidden accessing /api/v1/admin/ping")
    void shouldRejectStudentAccessToAdminPing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/ping"));
    }

    @Test
    @DisplayName("PAPER_SETTER role receives 403 Forbidden accessing /api/v1/centre/ping")
    void shouldRejectPaperSetterAccessToCentrePing() throws Exception {
        mockMvc.perform(get("/api/v1/centre/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("EXAM_OPERATOR role receives 403 Forbidden accessing /api/v1/authority/ping")
    void shouldRejectOperatorAccessToAuthorityPing() throws Exception {
        mockMvc.perform(get("/api/v1/authority/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_OPERATOR"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("STUDENT role receives 403 Forbidden accessing /api/v1/audit/ping")
    void shouldRejectStudentAccessToAuditPing() throws Exception {
        mockMvc.perform(get("/api/v1/audit/ping")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
