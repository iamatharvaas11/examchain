package com.examchain.auth.config;

import com.examchain.core.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.Instant;

/**
 * Spring Security 7 / Spring Boot 4.1.x Resource Server configuration with strict RBAC enforcement.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakJwtConverter jwtConverter;

    @Autowired
    public SecurityConfig(KeycloakJwtConverter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/health", "/api/v1/health/**").permitAll()
                        .requestMatchers("/api/v1/verify/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Role-scoped domain endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/authority/**").hasAnyRole("EXAM_AUTHORITY", "CONTROLLER")
                        .requestMatchers("/api/v1/setter/**").hasRole("PAPER_SETTER")
                        .requestMatchers("/api/v1/centre/**").hasAnyRole("CENTRE_ADMIN", "EXAM_OPERATOR")
                        .requestMatchers("/api/v1/operator/**").hasRole("EXAM_OPERATOR")
                        .requestMatchers("/api/v1/audit/**").hasAnyRole("AUDITOR", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/student/**").hasRole("STUDENT")

                        // Authenticated user profile
                        .requestMatchers("/api/v1/auth/me").authenticated()

                        // Default catch-all
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String message = authException != null && authException.getMessage() != null
                    ? authException.getMessage() : "Authentication required";
            String json = """
                    {"status":401,"error":"Unauthorized","message":"Authentication required: %s","path":"%s","timestamp":"%s","validationErrors":null}
                    """.formatted(
                            escape(message),
                            escape(request.getRequestURI()),
                            Instant.now().toString()
                    );
            response.getWriter().write(json);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String json = """
                    {"status":403,"error":"Forbidden","message":"Access denied: Insufficient privileges for this resource","path":"%s","timestamp":"%s","validationErrors":null}
                    """.formatted(
                            escape(request.getRequestURI()),
                            Instant.now().toString()
                    );
            response.getWriter().write(json);
        };
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"");
    }
}
