package com.likelion.olion.domain.reflectionshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reflection-share")
public record ReflectionShareProperties(
        String storage,
        long presignedUrlMinutes
) {
    public ReflectionShareProperties {
        if (presignedUrlMinutes <= 0) {
            presignedUrlMinutes = 10;
        }
    }
}
