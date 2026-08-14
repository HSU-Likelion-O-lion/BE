package com.likelion.olion.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.annotation.PostConstruct;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of();

    public List<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
            return List.of();
        }
        return allowedOrigins.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @PostConstruct
    void validate() {
        List<String> origins = getAllowedOrigins();
        if (origins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins must not be empty");
        }
        if (origins.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("app.cors.allowed-origins does not support wildcard origins");
        }
        origins.forEach(this::validateOrigin);
    }

    private void validateOrigin(String origin) {
        try {
            URI uri = new URI(origin);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawPath() != null && !uri.getRawPath().isEmpty()
                    || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalStateException("Invalid CORS origin: " + origin);
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid CORS origin: " + origin, exception);
        }
    }
}
