package com.examchain;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Test configuration providing a mock JwtDecoder for the test profile
 * so tests do not require a live Keycloak instance.
 */
@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }
}
