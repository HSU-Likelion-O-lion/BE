package com.likelion.olion.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    @Test
    void corsConfigurationSourceUsesAllowedOriginsFromProperties() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        SecurityConfig securityConfig = new SecurityConfig(null, null, properties);

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "http://localhost:3000");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
