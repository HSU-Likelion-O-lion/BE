package com.likelion.olion.domain.reflectionshare.service;

import java.time.Instant;

public record ReflectionShareRenderRequest(
        Long themeId,
        String content,
        String nickname,
        String profileImageUrl,
        Instant reflectionCreatedAt
) {
}
